package com.meiseguo.api.service;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;
import com.meiseguo.api.strategy.B;
import com.meiseguo.api.strategy.INPUT;
import com.meiseguo.api.strategy.Strategy;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class StrategyService implements StrategyApi {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Action> actions(String operator, double price) {
        Operator op = mongoTemplate.findOne(new Query(Criteria.where("operator").is(operator)), Operator.class);
        assert op != null;
        Strategy strategy = get(op);
        List<Action> actions = strategy.apply(new INPUT(price));
        if(!actions.isEmpty()) {
            mongoTemplate.insert(actions, Action.class);
        }
        return actions;
    }

    @Override
    public Strategy get(Operator operator) {
        Setting setting = mongoTemplate.findOne(new Query(Criteria.where("setting").is(operator.setting).and("strategy").is(operator.strategy)), Setting.class);
        Safety safety = mongoTemplate.findOne(new Query(Criteria.where("setting").is(operator.setting).and("strategy").is(operator.strategy)), Safety.class);
        Asset asset = mongoTemplate.findOne(new Query(Criteria.where("operator").is(operator.operator).and("ccy").is(operator.ccy)), Asset.class);
        Account account = mongoTemplate.findOne(new Query(Criteria.where("account").is(operator.account)), Account.class);
        assert account != null;
        assert setting != null;
        assert safety != null;
        if ("B".equals(operator.strategy)) {
            return new B(operator, setting, safety, asset, account, this);
        }
        return null;
    }

    @Override
    public List<Action> pendingActions(Operator operator, String type, String side) {
        List<Action> pending = mongoTemplate.find(new Query(Criteria.where("operator").is(operator.operator).and("account").is(operator.account).and("ccy").is(operator.ccy).and("type").is(type).and("side").is(side).and("status").in(ActionStatus.init.name(), ActionStatus.live.name())), Action.class);
        // 如果1分钟还没请求OKX下单，这种就标记为取消
        pending.stream()
                .filter(action -> ActionStatus.init.name().equals(action.status))
                .filter(action -> System.currentTimeMillis() - action.millis > TimeUnit.MINUTES.toMillis(1))
                .forEach(action -> {
                    action.setStatus(ActionStatus.canceled.name());
                    action.setUpdatetime(LocalDateTime.now());
                    save(action);
                });
        return pending.stream().filter(action -> !ActionStatus.canceled.name().equals(action.status)).collect(Collectors.toList());
    }

    @Override
    public List<Action> actionList(Operator operator, String type, String side, String status) {
        return mongoTemplate.find(new Query(Criteria.where("operator").is(operator.operator).and("account").is(operator.account).and("ccy").is(operator.ccy).and("type").is(type).and("side").is(side).and("status").is(status)), Action.class);
    }

    @Override
    public Optional<Action> latestAction(Operator operator, String type, String side, String status) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where("operator").is(operator.operator).and("account").is(operator.account).and("ccy").is(operator.ccy).and("type").is(type).and("side").is(side).and("status").is(status)).with(Sort.by(Sort.Order.desc("sn"))).limit(1), Action.class));
    }

    @Override
    public void update(String ordId, String sn, String status) {
        Action action = mongoTemplate.findOne(new Query(Criteria.where("sn").is(new ObjectId(sn))), Action.class);
        assert action != null;
        action.setOrder(ordId);
        action.setStatus(status);
        mongoTemplate.save(action);
    }

    @Override
    public void save(Action action) {
        mongoTemplate.save(action);
    }

    @Override
    public void save(List<Closed> result) {
        mongoTemplate.insert(result, Closed.class);
    }

    @Override
    public void save(Setting setting) {
        mongoTemplate.save(setting);
    }

    @Override
    public void save(Alert alert) {
        mongoTemplate.save(alert);
    }

    @Override
    public void save(Statistic statistic) {
        mongoTemplate.save(statistic);
    }

}
