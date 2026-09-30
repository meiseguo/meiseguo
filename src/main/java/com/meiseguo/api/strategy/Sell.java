package com.meiseguo.api.strategy;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;
import com.meiseguo.api.utils.CryptoUtil;
import com.meiseguo.api.utils.PagesUtil;

import java.util.List;
import java.util.Optional;

public class Sell extends Strategy {
    public Sell(Operator operator, Status status, Setting setting, Safety safety, Asset asset, Account account, StrategyApi api) {
        super(StrategyType.sell, operator, status, setting, safety, asset, account, api);
    }

    @Override
    public boolean safe(double price) {
        return guess.test(Case.when(mode, CryptoUtil.now() + " " + price + " <= " + safety.alive + "安全").is(price <= safety.alive));
    }

    @Override
    public Optional<Action> open(Input input) {
        if (isForbidden()) {
            return Optional.empty();
        }
        Relax relax = api.relax(operator.operator, RelaxReason.open);
        Record marker = api.marker(input.ccy);
        Record record = api.record(input.ccy);
        List<Action> actions = investment();
        if (isLow(marker) || isLow(record) || isTooMuch(input) || isRelax(relax) || sameAction(input, setting.winRatio)) {
            return Optional.empty();
        }
        if (marker.latest() > STOP_WIN || actions.isEmpty()) {
            relax.calm(setting.timeGap);
            return Optional.of(openAction(input));
        }
        switch (mode) {
            case Invest:
                if (isHigh(marker)) {
                    relax.calm(setting.timeGap);
                    return Optional.of(openAction(input));
                }
                break;
            case Rescue:
                if (isHigh(marker) && isHigh(record)) {
                    relax.calm(setting.timeGap);
                    return Optional.of(openAction(input));
                }
                break;
            default:
                break;
        }
        return Optional.empty();
    }

    private Action openAction(Input input) {
        Action action = newAction();
        action.buy(input, type.open(operator.openAmt, input.price));
        return action;
    }

    private Action closeAction(Input input, Action open) {
        api.relax(operator.operator, RelaxReason.open).calm(setting.timeGap);
        Action action = newAction();
        if (operator.zhang == 1 && open.price < input.price) {
            action.sell(input, type.close(open, input.price));
        } else {
            action.sell(input, open.amount);
        }
        api.save(open.closedBy(action));
        api.save(open);
        // 跟单
        Optional<Follow> follow = api.getFollow(ActionRelation.open, open.getSn());
        if (follow.isPresent()) {
            Follow todo = follow.get();
            todo.setClose(action.getSn().toString());
            todo.setStatus(ActionStatus.closed.name());
            api.save(todo);
        }
        return action;
    }

    @Override
    public Optional<Action> close(Input input) {
        Record marker = api.marker(input.ccy);
        List<Action> actions = investment();
        updateStatus(api.record(input.ccy), api.marker(input.ccy), actions, input);
        if (isLow(marker)) {
            // 记录最低标记价
            for (Action action : actions) {
                double mark = input.price;
                if (mark < action.mark) {
                    action.setMark(mark);
                    api.save(action);
                }
            }
            // 低处标记止盈
            if (isLow(api.record(input.ccy))) {
                for(Action action : actions) {
                    if (action.winRatio(input) > setting.winRatio) {
                        action.setStopWin(1);
                        api.save(action);
                    }
                }
            }
        }
        Relax relax = api.relax(operator.operator, RelaxReason.close);
        if (isRelax(relax)) {
            return Optional.empty();
        }
        if(isNoLoss()) {
            if(guess.test(Case.when(mode, "均线之上：" + safety.average).is(input.price > safety.average))) {
                return Optional.empty();
            }
        }
        Optional<Action> close = actions.stream()
                .filter(action -> action.stopWin == 1)
                .filter(action -> {
                    double winRatio = action.winRatio(input);
                    double turnRatio = action.turnRatio(input);
                    if (winRatio > STOP_WIN) {
                        return guess.test(Case.when(mode, "提前止盈：" + PagesUtil.percent(winRatio)).is(true));
                    }
                    if (winRatio > setting.winRatio) {
                        if (input.price < action.mark) {
                            return guess.test(Case.when(mode, "网格之外：" + PagesUtil.percent(winRatio) + "回撤：" + PagesUtil.percent(turnRatio)).is(turnRatio < Math.max(setting.drawdownRatio, -0.001)));
                        } else {
                            return guess.test(Case.when(mode, "网格之内：" + PagesUtil.percent(winRatio) + "回撤：" + PagesUtil.percent(turnRatio)).is(true));
                        }
                    }
                    return winRatio > setting.minRatio && turnRatio < Math.min(setting.drawdownRatio, -0.001) && winRatio > Math.abs(turnRatio);
                })
                .findFirst();
        if (close.isPresent()) {
            Action open = close.get();
            double winRatio = open.winRatio(input);
            if (guess.test(Case.when(mode, "平仓：" + PagesUtil.percent(winRatio) + "回撤：" + PagesUtil.percent(open.turnRatio(input)) + "设置：" + PagesUtil.percent(setting.drawdownRatio) + "止损：" + open.stopLoss + "止盈：" + open.stopWin).is(true))) {
                Action action = closeAction(input, open);
                flipStatus(winRatio > 0);
                relax.calm(setting.timeGapClose);
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }
}
