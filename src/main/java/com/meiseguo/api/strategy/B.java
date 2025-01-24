package com.meiseguo.api.strategy;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class B extends Strategy {
    public B(Setting setting, Safety safety, Mode mode, Asset asset, Account account, StrategyApi api) {
        super(setting, safety, mode, asset, account, api);
    }

    @Override
    public boolean safe(double price) {
        // 当前价太靠近危险线
        if (price - safety.danger < setting.priceDiff) {
            return false;
        }
        // 安全边界低于危险线
        if (safety.safety < safety.danger) {
            return false;
        }
        // 当前价低于安全边界
        if (price < safety.safety) {
            return false;
        }
        // 当前价在安全边界之上 且 安全边界在生命线之上 且 安全边界在危险线之上
        return (price >= safety.safety && safety.safety > safety.alive && safety.safety > safety.danger);
    }

    @Override
    public double margin(double price) {
        return (price - safety.safety) / (safety.safety - safety.danger);
    }

    @Override
    public Optional<Action> sell(INPUT input) {
        List<Action> actions = api.actionList(account.account, asset.ccy, buy, deal);
        Optional<Action> latest = api.latestAction(account.account, asset.ccy, sell, deal);

        switch (mode) {
            case Rescue:
                // 如果是拯救模式，能卖的全部卖了。
                List<Action> rescueList = actions.stream()
                        .filter(action -> action.winRatio(input) >= setting.minRatio)
                        .collect(Collectors.toList());
                if (rescueList.isEmpty()) {
                    return Optional.empty();
                }
                double amount = rescueList.stream().mapToDouble(action -> action.amount).sum();
                Action rescue = new Action(account, asset, buy);
                rescue.sell(input.price, amount);
                List<Closed> closed = rescueList.stream().map(action -> action.close(rescue)).collect(Collectors.toList());
                rescueList.forEach(action -> api.save(action));
                api.save(closed);
                return Optional.of(rescue);
            case Invest:
                if(latest.isPresent()) {
                    Action sell = latest.get();
                    double priceGap = input.price - sell.price;
                    long timeGap = System.currentTimeMillis() - sell.millis;
                    // 投资模式下，如果价格涨的不是很高，或者相隔时间不长，就先不动。
                    if(priceGap < setting.timeGapMin && timeGap < setting.priceDiffMin) {
                        return Optional.empty();
                    }
                }
                List<Action> investList = actions.stream()
                        .filter(action -> action.winRatio(input) >= setting.winRatio)
                        .sorted(Comparator.comparingDouble(action -> action.price))
                        .collect(Collectors.toList());
                if (investList.isEmpty()) {
                    return Optional.empty();
                }
                Action cheap = investList.get(0);
                Action invest = new Action(account, asset, buy);
                invest.sell(input.price, cheap.amount);
                cheap.close(invest);
                api.save(cheap);
                return Optional.of(invest);
            case Harvest:
                if(latest.isPresent()) {
                    Action sell = latest.get();
                    if(input.price < sell.price) {
                        // 如果是丰收模式，不涨不卖。
                        return Optional.empty();
                    }
                }
                double harvestRatio = setting.winRatio * setting.goldenRatio;
                List<Action> harvestList = actions.stream()
                        .filter(action -> action.winRatio(input) >= harvestRatio)
                        .sorted(Comparator.comparingDouble(action -> action.amount))
                        .collect(Collectors.toList());
                if (harvestList.isEmpty()) {
                    return Optional.empty();
                }
                Action least = harvestList.get(0);
                Action harvest = new Action(account, asset, buy);
                harvest.sell(input.price, least.amount);
                least.close(harvest);
                api.save(least);
                return Optional.of(harvest);
            case Freeze:
                return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * TODO 同一价格水平的未结订单要限制数量吗？
     * @param current 当前价
     * @return 可能买
     */
    @Override
    public Optional<Action> buy(INPUT current) {
        Optional<Action> bought = api.latestAction(account.account, asset.ccy, buy, deal);
        Optional<Action> sold = api.latestAction(account.account, asset.ccy, sell, deal);
        switch (mode) {
            case Invest:
            case Harvest:
                // 如果清仓之后，这里有BUG
                if (!bought.isPresent()) {
                    Action initBuy = new Action(account, asset, buy);
                    initBuy.buy(current.price, setting.unitBuyAmt);
                    return Optional.of(initBuy);
                }
                Action lastBuy = bought.get();
                double priceDiff = lastBuy.price - current.price;
                long timeGap = System.currentTimeMillis() - lastBuy.millis;
                // 投资阶段，价格跌幅满足条件，时间跨度满足条件，就可以买买买
                if (priceDiff >= setting.priceDiff && timeGap >= setting.priceDiffMin) {
                    Action buyTimeGap = new Action(account, asset, buy);
                    buyTimeGap.buy(current.price, lastBuy.amount * setting.goldenRatio);
                    return Optional.of(buyTimeGap);
                }

                if (timeGap >= setting.timeGap && priceDiff >= setting.timeGapMin) {
                    Action buyPriceDiff = new Action(account, asset, buy);
                    buyPriceDiff.buy(current.price, setting.unitBuyAmt);
                    return Optional.of(buyPriceDiff);
                }

                if (!sold.isPresent()) {
                    return Optional.empty();
                }
                // 如果还没接近天花板，那是不是还可以投资
                Action lastSell = sold.get();
                double priceDrop = lastSell.price - current.price;
                // 还有一种情况是在上涨行情，刚卖掉了一笔，然后回落了。可以买一笔
                if (lastSell.millis > lastBuy.millis && timeGap >= setting.timeGap && priceDrop >= setting.priceDiff) {
                    Action buyPriceDiff = new Action(account, asset, buy);
                    buyPriceDiff.buy(current.price, setting.unitBuyAmt);
                    return Optional.of(buyPriceDiff);
                }

            case Rescue:
            case Freeze:
                return Optional.empty();
        }
        return Optional.empty();
    }
}
