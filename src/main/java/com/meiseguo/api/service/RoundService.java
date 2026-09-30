package com.meiseguo.api.service;

import com.meiseguo.api.pojo.*;
import com.meiseguo.api.utils.CryptoUtil;
import com.meiseguo.api.vo.RoundVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service

public class RoundService {
    @Autowired
    private ConfigService configService;
    @Autowired
    private MongoTemplate mongoTemplate;
    private final AtomicInteger index = new AtomicInteger(0);

    /**
     * 开一局
     *
     * @param operator 操作员
     * @param type     做多做空
     * @param unit 单笔
     * @return roundVO
     */
    public RoundVo createRound(Operator operator, String type, Double unit) {
        if(index.get() == 0) {
            long size = mongoTemplate.count(new Query(Criteria.where("deleted").is(0)), Round.class);
            index.set((int) size);
        }
        Round round = new Round();
        round.setRound(CryptoUtil.today() + "第" + index.incrementAndGet() + "局");
        round.setCcy(operator.ccy);
        round.setType(type);
        round.setUnit(unit);
        round.setOperator(operator.operator);
        round.setStatus(ActionStatus.init.name());
        mongoTemplate.save(round);
        return new RoundVo(round, new ArrayList<>());
    }

    public List<RoundVo> getRounds() {
        List<Round> rounds = mongoTemplate.find(new Query(Criteria.where("deleted").is(0)).with(Sort.by("sn").descending()), Round.class);
        List<RoundVo> result = new ArrayList<>();
        for (Round round : rounds) {
            if(ActionStatus.closed.name().equals(round.getStatus())) {
                continue;
            }
            List<Follow> followList = mongoTemplate.find(new Query(Criteria.where("round").is(round.round)).with(Sort.by("sn").descending()), Follow.class);
            result.add(new RoundVo(round, followList));
        }
        return result;
    }

    /**
     * 跟单
     * @param sn 场次
     * @return roundVO
     */
    public RoundVo follow(String sn) {
        Round round = mongoTemplate.findOne(new Query(Criteria.where("sn").is(sn)), Round.class);
        assert round != null;
        List<Follow> followList = mongoTemplate.find(new Query(Criteria.where("round").is(round.round)), Follow.class);
        if(ActionStatus.closed.name().equals(round.status)) {
            return new RoundVo(round, followList);
        }
        Follow follow = new Follow();
        follow.setCcy(round.ccy);
        follow.setSide(round.type);
        follow.setType(round.type);
        follow.setRound(round.round);
        follow.setOperator(round.operator);
        follow.setStatus(ActionStatus.init.name());
        follow.setStrategy(StrategyType.buy.name().equals(round.type) ? Strategies.low.name() : Strategies.high.name());
        follow.setAmount(round.unit);
        follow.setIndex(followList.size() + 1);
        mongoTemplate.save(follow);
        round.setStatus(ActionStatus.live.name());
        mongoTemplate.save(round);
        followList.add(follow);
        return new RoundVo(round, followList);
    }

    public RoundVo closing(String sn) {
        Round round = mongoTemplate.findOne(new Query(Criteria.where("sn").is(sn)), Round.class);
        assert round != null;

        List<Follow> followList = mongoTemplate.find(new Query(Criteria.where("round").is(round.round)), Follow.class);
        List<Action> actionList = mongoTemplate.find(new Query(Criteria.where("operator").is(round.operator).and("status").is(ActionStatus.filled.name()).and("type").is(round.type).and("side").is(round.type)), Action.class);
        followList.forEach(follow -> {
            follow.setStatus(ActionStatus.closed.name());
            follow.setUpdatetime(LocalDateTime.now());
            mongoTemplate.save(follow);
        });
        Record record = StrategyService.records.get(round.ccy);
        Action closed =  new Action();
        closed.setPrice(record.current.price);
        closed.setStatus(ActionStatus.closed.name());
        closed.setOperator(round.operator);
        closed.setType(round.type);
        closed.setCcy(round.ccy);
        closed.setAmount(actionList.stream().mapToDouble(Action::getAmount).sum());
        closed.setReason("手动清算");
        mongoTemplate.save(closed);
        actionList.forEach(action -> {
            action.setStatus(ActionStatus.closed.name());
            action.setUpdatetime(LocalDateTime.now());
            Closed close = action.closedBy(closed);
            mongoTemplate.save(close);
            mongoTemplate.save(action);
        });

        double amount = followList.stream().filter(follow -> !ObjectUtils.isEmpty(follow.open)).mapToDouble(follow -> follow.amount).sum();
        round.setAmount(amount);
        round.setStatus(ActionStatus.closed.name());
        mongoTemplate.save(round);
        return new RoundVo(round, followList);
    }

    public void now(String sn) {
        Follow follow = mongoTemplate.findOne(new Query(Criteria.where("sn").is(sn)), Follow.class);
        if(!ObjectUtils.isEmpty(follow) && !Strategies.now.name().equals(follow.strategy)) {
            follow.setStrategy(Strategies.now.name());
            // Record record = StrategyService.records.get(follow.ccy);
            // follow.setStrategy(Strategies.fix.name());
            // follow.setPrice(record.current.price);
            follow.setUpdatetime(LocalDateTime.now());
            mongoTemplate.save(follow);
        }
    }

    public void reduce(String sn) {
        Follow follow = mongoTemplate.findOne(new Query(Criteria.where("sn").is(sn)), Follow.class);
        if(!ObjectUtils.isEmpty(follow)
                && !ActionStatus.closing.name().equals(follow.status)
                && !ActionStatus.closed.name().equals(follow.status)
                && !ActionStatus.filled.name().equals(follow.status)) {
            follow.setStatus(ActionStatus.closing.name());
            follow.setUpdatetime(LocalDateTime.now());
            mongoTemplate.save(follow);
        }
    }
}
