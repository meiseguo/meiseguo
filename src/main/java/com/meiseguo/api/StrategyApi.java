package com.meiseguo.api;

import com.meiseguo.api.pojo.*;
import com.meiseguo.api.strategy.Strategy;

import java.util.List;
import java.util.Optional;

public interface StrategyApi {

    Strategy get(Operator operator);
    List<Action> pendingActions(Operator operator, String type, String side);
    List<Action> actionList(Operator operator, String type, String side, String status);
    Optional<Action> latestAction(Operator operator, String type, String side, String status);
    void update(String ordId, String sn, String status);
    void save(Action result);
    void save(List<Closed> result);
    void save(Setting setting);
    void save(Alert alert);
}
