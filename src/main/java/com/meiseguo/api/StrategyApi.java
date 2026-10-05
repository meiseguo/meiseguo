package com.meiseguo.api;

import com.meiseguo.api.pojo.*;
import com.meiseguo.api.strategy.Guess;
import com.meiseguo.api.strategy.Strategy;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface StrategyApi {
    void alert(String from, Operator operator, String message);
    Relax relax(String operator, RelaxReason reason);
    Record record(String ccy);
    Record marker(String ccy);
    Optional<Follow> getFollow(ActionRelation which, ObjectId sn);
    Optional<Operator> getPartner(Operator operator);
    Strategy get(Operator operator);
    Action get(String sn);
    List<Operator> allOperators(String account);
    List<Action> pendingActions(Operator operator, StrategyType type);
    List<Action> actionList(Operator operator, StrategyType type, String side, String status);
    List<Follow> followList(Operator operator, StrategyType type, String side, String status);
    Optional<Closed> lastClosed(Operator operator, StrategyType type);
    void update(String ordId, String sn, String status);
    void save(Action action);
    void save(Closed close);
    void save(Safety safety);
    void ticker(String instType, double price, long ts);
    void save(Guess guess);
    void save(Follow follow);
    void save(Operator operator);
    void save(Status status);
    void save(Setting setting);
}
