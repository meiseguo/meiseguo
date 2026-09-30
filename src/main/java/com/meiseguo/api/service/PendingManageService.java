package com.meiseguo.api.service;

import com.meiseguo.api.API;
import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.meiseguo.api.pojo.*;
import com.meiseguo.api.strategy.Input;
import com.meiseguo.api.utils.CryptoUtil;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
public class PendingManageService implements IManageService {

    private MongoTemplate mongoTemplate;

    @Override
    public Reply page(Class<?> clazz, API api, PageDto dto) {
        Map<String, Record> records = StrategyService.records;
        List<Pending> list = new ArrayList<>();
        List<Action> pendingBuy = pendingAction(OrderSide.buy, StrategyType.buy);
        pendingBuy.forEach(action -> {
            Pending pending = new Pending(action, records.get(action.ccy).current);
            list.add(pending);
        });
        List<Action> pendingSell = pendingAction(OrderSide.sell, StrategyType.sell);
        pendingSell.forEach(action -> {
            Pending pending = new Pending(action, records.get(action.ccy).current);
            list.add(pending);
        });
        return Reply.success(list).total(list.size());
    }

    private List<Action> pendingAction(OrderSide side, StrategyType type) {
        return mongoTemplate.find(new Query(Criteria.where("side").is(side.name()).and("type").is(type.name()).and("status").is(ActionStatus.filled.name())), Action.class);
    }

    public Map<String, Invest> getInvest() {
        Map<String, Invest> invest = new HashMap<>();
        List<Invest> investList = mongoTemplate.find(new Query(Criteria.where("updatetime").gt(LocalDate.now().atStartOfDay())), Invest.class);
        if (investList.isEmpty()) {
            invest.put(InvestType.all.name(), new Invest(InvestType.all.name(), InvestType.all.desc()));
            invest.put(InvestType.day.name(), new Invest(InvestType.day.name(), CryptoUtil.today()));
            invest.put(InvestType.time.name(), new Invest(InvestType.time.name(), InvestType.time.desc()));
            return invest;
        }
        Invest all = investList.stream().filter(i -> InvestType.all.name().equals(i.type)).findFirst().orElse(new Invest(InvestType.all.name(), InvestType.all.desc()));
        Invest day = investList.stream().filter(i -> InvestType.day.name().equals(i.type) && CryptoUtil.today().equals(i.name)).findFirst().orElse(new Invest(InvestType.day.name(), CryptoUtil.today()));
        Invest time = investList.stream().filter(i -> InvestType.time.name().equals(i.type)).max(Comparator.comparing(Invest::getUpdatetime)).orElse(new Invest(InvestType.time.name(), InvestType.time.desc()));
        invest.put(InvestType.all.name(), all);
        invest.put(InvestType.day.name(), day);
        invest.put(InvestType.time.name(), time);
        update(invest);
        return invest;
    }

    public void update(Map<String, Invest> invest) {
        if (StrategyService.records.isEmpty()) return;
        List<Action> pendingBuy = pendingAction(OrderSide.buy, StrategyType.buy);
        List<Action> pendingSell = pendingAction(OrderSide.sell, StrategyType.sell);
        List<Action> pending = new ArrayList<>();
        pending.addAll(pendingBuy);
        pending.addAll(pendingSell);
        double value = pending.stream().mapToDouble(Action::getValue).sum();
        double amount = pending.stream().mapToDouble(Action::getAmount).sum();

        double lossB = pendingBuy.stream().mapToDouble(action -> {
            Input input = StrategyService.records.get(action.ccy).current;
            if (input == null || input.price > action.price) {
                return 0;
            }
            return action.winRatio(input) * action.price * action.amount;
        }).sum();
        double lossS = pendingSell.stream().mapToDouble(action -> {
            Input input = StrategyService.records.get(action.ccy).current;
            if (input == null || input.price < action.price) {
                return 0;
            }
            return action.winRatio(input) * action.price * action.amount;
        }).sum();
        double lossBuy = pendingBuy.stream().mapToDouble(action -> {
            Input input = StrategyService.records.get(action.ccy).current;
            if (input == null || input.price > action.price) {
                return 0;
            }
            return action.amount;
        }).sum();
        double lossSell = pendingSell.stream().mapToDouble(action -> {
            Input input = StrategyService.records.get(action.ccy).current;
            if (input == null || input.price < action.price) {
                return 0;
            }
            return action.amount;
        }).sum();
        invest.values().stream().filter(i -> InvestType.time.name().equals(i.type)).forEach(i -> {
            i.setMinValue(Double.MAX_VALUE);
            i.setMaxValue(Double.MIN_VALUE);
            i.setMinAmount(Long.MAX_VALUE);
            i.setMaxAmount(Long.MIN_VALUE);
        });
        invest.values().forEach(i -> {
            updateInvest(i, value, amount);
            updateInvest(i, lossB + lossS, -lossBuy - lossSell);
        });
    }

    public void updateInvest(Invest invest, double value, double amount) {
        boolean update = false;
        if (value < invest.minValue) {
            invest.setMinValue(value);
            update = true;
        }
        if (value > invest.maxValue) {
            invest.setMaxValue(value);
            update = true;
        }
        if (amount < invest.minAmount) {
            invest.setMinAmount(amount);
            update = true;
        }
        if (amount > invest.maxAmount) {
            invest.setMaxAmount(amount);
            update = true;
        }
        if (update) {
            if (InvestType.day.name().equals(invest.type)) {
                if (!CryptoUtil.today().equals(invest.name)) {
                    invest.setSn(new ObjectId());
                    invest.setName(CryptoUtil.today());
                    invest.setMinValue(value);
                    invest.setMaxValue(value);
                    invest.setMinAmount(amount);
                    invest.setMaxAmount(amount);
                    invest.setCreatetime(LocalDateTime.now());
                }
            }
            invest.setUpdatetime(LocalDateTime.now());
            mongoTemplate.save(invest);
        }
    }

    @Override
    public Reply update(Class<?> clazz, API api, UpdateDto dto) {
        return Reply.fail();
    }

    @Override
    public Reply insert(Class<?> clazz, API api, Object object) {
        return Reply.fail();
    }

    @Override
    public Reply delete(Class<?> clazz, API api, String sn) {
        return Reply.fail();
    }
}
