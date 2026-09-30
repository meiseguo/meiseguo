package com.meiseguo.api.strategy;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;
import com.meiseguo.api.utils.PagesUtil;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class Strategy implements Function<Input, List<Action>> {
    public StrategyType type;
    public Operator operator;
    public Status status;
    public Setting setting;
    public Safety safety;
    public Mode mode;
    public Asset asset;
    public Account account;
    public StrategyApi api;
    public Guess guess;
    public static final double STOP_WIN = 0.02;

    public Strategy(StrategyType type, Operator operator, Status status, Setting setting, Safety safety, Asset asset, Account account, StrategyApi api) {
        this.operator = operator;
        this.status = status;
        this.setting = setting;
        this.safety = safety;
        this.mode = Mode.valueOf(operator.mode);
        this.asset = asset;
        this.account = account;
        this.api = api;
        this.type = type;
        this.guess = new Guess(operator);
    }

    public abstract boolean safe(double price);

    public abstract Optional<Action> open(Input input);

    public abstract Optional<Action> close(Input input);

    /**
     * 跟单
     *
     * @param input 当前价
     * @return 跟单
     */
    public Optional<Action> follow(Input input) {
        // 减仓ing
        List<Follow> closingBuy = followList(OrderSide.buy, ActionStatus.closing);
        Optional<Follow> min = closingBuy.stream().min(Comparator.comparingDouble(Follow::getPrice));
        if (guess.test(Case.when(mode, "卖出减仓").is(min.isPresent()))) {
            assert min.isPresent();
            Follow follow = min.get();
            Action closed = api.get(follow.open);
            if(!ObjectUtils.isEmpty(closed)) {
                double amount = follow.getAmount();
                Action action = newAction();
                action.sell(input, amount);
                action.setFollow(1);
                follow.setClose(action.getSn().toString());
                follow.setStatus(ActionStatus.closed.name());
                follow.setPrice(input.price);
                api.save(closed.closedBy(action));
                api.save(closed);
                api.save(follow);
                return Optional.of(action);
            }
        }
        List<Follow> closingSell = followList(OrderSide.sell, ActionStatus.closing);
        Optional<Follow> max = closingSell.stream().max(Comparator.comparingDouble(Follow::getPrice));
        if (guess.test(Case.when(mode, "买入减仓").is(max.isPresent()))) {
            assert max.isPresent();
            Follow follow = max.get();
            Action closed = api.get(follow.open);
            double amount = follow.getAmount();
            Action action = newAction();
            action.buy(input, amount);
            action.setFollow(1);
            follow.setClose(action.getSn().toString());
            follow.setStatus(ActionStatus.closed.name());
            follow.setPrice(input.price);
            api.save(closed.closedBy(action));
            api.save(closed);
            api.save(follow);
            return Optional.of(action);
        }
        // 跟单
        List<Follow> followBuy = followList(OrderSide.buy, ActionStatus.init);
        List<Follow> followSell = followList(OrderSide.sell, ActionStatus.init);
        if (guess.test(Case.when(mode, "没有跟单").is(ObjectUtils.isEmpty(followBuy) && ObjectUtils.isEmpty(followSell)))) {
            return Optional.empty();
        }
        Record record = api.record(input.ccy);
        if (guess.test(Case.when(mode, "买入跟单：" + followBuy.size()).is(!ObjectUtils.isEmpty(followBuy)))) {
            Optional<Follow> followB = followBuy.stream().filter(follow -> {
                Strategies type = Strategies.valueOf(follow.strategy);
                switch (type) {
                    case now:
                        return true;
                    case fix:
                        return input.price <= follow.price;
                    case low:
                        return isLow(record);
                }
                return false;
            }).findFirst();
            if (guess.test(Case.when(mode, "跟单买入：" + followB.isPresent()).is(followB.isPresent()))) {
                assert followB.isPresent();
                Follow follow = followB.get();
                double amount = follow.getAmount();
                Action action = newAction();
                action.buy(input, amount);
                action.setFollow(1);
                follow.setOpen(action.getSn().toString());
                follow.setStatus(ActionStatus.live.name());
                follow.setPrice(input.price);
                api.save(follow);
                return Optional.of(action);
            }
        }

        if (guess.test(Case.when(mode, "卖出跟单：" + followSell.size()).is(!ObjectUtils.isEmpty(followSell)))) {
            Optional<Follow> followS = followSell.stream().filter(follow -> {
                Strategies type = Strategies.valueOf(follow.strategy);
                switch (type) {
                    case now:
                        return true;
                    case fix:
                        return input.price >= follow.price;
                    case high:
                        return isHigh(record);
                }
                return false;
            }).findFirst();
            if (guess.test(Case.when(mode, "跟单卖出：" + followS.isPresent()).is(followS.isPresent()))) {
                assert followS.isPresent();
                Follow follow = followS.get();
                double amount = follow.getAmount();
                Action action = newAction();
                action.sell(input, amount);
                action.setFollow(1);
                follow.setOpen(action.getSn().toString());
                follow.setStatus(ActionStatus.live.name());
                follow.setPrice(input.price);
                api.save(follow);
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }

    public Action newAction() {
        return new Action(operator, type);
    }

    /**
     * 策略应用
     *
     * @param input 输入
     */
    @Override
    public List<Action> apply(Input input) {
        List<Action> pending = pendingActions();
        if (guess.test(Case.when(mode, "正在委托").is(!pending.isEmpty()))) {
            api.save(guess);
            return Collections.emptyList();
        }
        Optional<Action> follow = follow(input);
        if (follow.isPresent()) {
            return accept(follow.get());
        }
        if (safe(input.getPrice())) {
            Optional<Action> closeAction = close(input);
            if (closeAction.isPresent()) {
                return accept(closeAction.get());
            }
            Optional<Action> openAction = open(input);
            if (openAction.isPresent()) {
                return accept(openAction.get());
            }
        }
        api.save(guess);
        return Collections.emptyList();
    }

    public void updateStatus(Record record, Record marker, List<Action> actions, Input input) {
        long winCount = actions.stream().filter(action -> action.winRatio(input) > 0).count();
        long lossCount = actions.stream().filter(action -> action.winRatio(input) < 0).count();
        double winValue = actions.stream().filter(action -> action.winRatio(input) > 0).mapToDouble(action -> gain(action, input)).sum();
        double lossValue = actions.stream().filter(action -> action.winRatio(input) < 0).mapToDouble(action -> gain(action, input)).sum();
        status.setWinCount(winCount);
        status.setLossCount(lossCount);
        status.setWinValue(winValue);
        status.setLossValue(lossValue);
        if (StrategyType.sell.equals(type)) {
            setting.setDrawdownRatio(BigDecimal.valueOf(marker.average(1)).setScale(7, RoundingMode.HALF_UP).negate().doubleValue());
            setting.setWinRatio(BigDecimal.valueOf(record.average(-1)).setScale(7, RoundingMode.HALF_UP).doubleValue());
        } else {
            setting.setDrawdownRatio(BigDecimal.valueOf(marker.average(-1)).setScale(7, RoundingMode.HALF_UP).negate().doubleValue());
            setting.setWinRatio(BigDecimal.valueOf(record.average(1)).setScale(7, RoundingMode.HALF_UP).doubleValue());
        }
        // 当天之内的投资出现浮亏N次，就缩小投资
        long todayLoss = actions.stream().filter(action -> input.millis - action.millis < TimeUnit.HOURS.toMillis(24)).filter(action -> action.winRatio(input) < 0).count();
        if (todayLoss >= setting.limitedCount) {
            operator.setOpenAmt(setting.unitAmt*0.3);
            api.alert("updateStatus", operator, "今日浮亏："+todayLoss+"，降级投资：" + operator.openAmt);
        }
        api.save(operator);
        api.save(setting);
        api.save(status);
    }

    public List<Action> accept(Action action) {
        String reason = guess.toString();
        action.setReason(reason);
        api.save(guess);
        return Collections.singletonList(action);
    }

    /**
     * 查询未成交订单列表
     *
     * @return 未成交订单列表
     */
    public List<Action> pendingActions() {
        return api.pendingActions(operator, type);
    }

    public List<Action> investment() {
        return api.actionList(operator, type, type.name(), ActionStatus.filled.name());
    }

    public List<Follow> followList(OrderSide side, ActionStatus status) {
        return api.followList(operator, type, side.name(), status.name());
    }

    public double gain(Action action, Input input) {
        return action.winRatio(input) * action.price * action.amount;
    }

    public boolean sameAction(Input input, double priceDiff) {
        long limited = investment().stream().filter(a -> input.millis - a.millis < TimeUnit.HOURS.toMillis(24)).filter(a -> Math.abs(a.winRatio(input)) < priceDiff).count();
        return guess.test(Case.when(mode, "相同订单：" + limited).is(limited > 0));
    }

    public boolean isForbidden() {
        return guess.test(Case.when(mode, "禁止开仓：" + mode).is(mode == Mode.Clear || mode == Mode.Freeze));
    }

    public boolean isRelax(Relax relax) {
        return guess.test(Case.when(mode, relax.reason + "限速：" + Duration.between(LocalDateTime.now(), relax.until).getSeconds() + "秒").is(relax.relax()));
    }

    public boolean isLow(Record record) {
        return guess.test(Case.when(mode, "局部最低：" + PagesUtil.percent(record.latest()) + " 反弹：" + PagesUtil.percent(record.decline()) + "及格：" + PagesUtil.percent(record.average(-1))).is(record.low()));
    }

    public boolean isHigh(Record record) {
        return guess.test(Case.when(mode, "局部最高：" + PagesUtil.percent(record.latest()) + " 反弹：" + PagesUtil.percent(record.decline()) + "及格：" + PagesUtil.percent(record.average(1))).is(record.high()));
    }

    public boolean timeout(Action action, Input input) {
        return input.millis - action.millis > setting.limitedTime;
    }

    public boolean isNoLoss() {
        return guess.test(Case.when(mode, "均线设置：" + setting.average).is(setting.average == 1));
    }

    public boolean isTrue(String reason, boolean bool) {
        return guess.test(Case.when(mode, reason).is(bool));
    }

    public boolean isTooMuch(Input input) {
        List<Action> actions = investment();
        double total = actions.stream().mapToDouble(Action::getValue).sum();
        double investedValue = actions.stream().filter(a -> Math.abs(a.winRatio(input)) < STOP_WIN).mapToDouble(Action::getValue).sum();
        return isTrue("短期超限：" + PagesUtil.numbers(investedValue) + "> " + setting.limitedValue, investedValue > setting.limitedValue) || isTrue("整体超限：" + PagesUtil.numbers(total) + ">" + asset.balance, total > asset.balance);
    }

    public void flipStatus(boolean win) {
        if (win) {
            if (StatusType.loss.name().equals(status.status)) {
                if (status.lossLossCount < status.lossMaxCount) {
                    status.setLossMaxCount(status.lossLossCount);
                }
                status.setLossLossCount(0);
            } else {
                if (status.winWinCount >= status.limitWinCount) {
                    api.alert("flipStatus", operator, "连续止盈：" + status.winWinCount + "/" + status.limitWinCount);
                }
            }
            operator.setOpenAmt(setting.openAmt * 3);
            api.alert("flipStatus", operator,"止盈升级：" + operator.openAmt);
            status.setWinWinCount(status.winWinCount + 1);
            status.setStatus(StatusType.win.name());
        } else {
            if (StatusType.win.name().equals(status.status)) {
                if (status.winWinCount > status.winMaxCount) {
                    status.setWinMaxCount(status.winWinCount);
                }
                status.setWinWinCount(0);
            } else {
                if (-status.lossLossCount >= status.limitLossCount) {
                    api.alert("flipStatus", operator, "连续止损：" + status.lossLossCount + "/" + status.limitLossCount);
                    operator.setStopLoss(1);
                }
            }
            operator.setOpenAmt(setting.unitAmt*0.3);
            api.alert("flipStatus", operator,"止损降级：" + operator.openAmt);
            status.setLossLossCount(status.lossLossCount - 1);
            status.setStatus(StatusType.loss.name());
        }
        api.save(operator);
        api.save(status);
    }
}