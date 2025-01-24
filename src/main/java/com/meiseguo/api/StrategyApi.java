package com.meiseguo.api;

import com.meiseguo.api.pojo.*;
import com.meiseguo.api.strategy.INPUT;
import com.meiseguo.api.strategy.Strategy;

import java.util.List;
import java.util.Optional;

public interface StrategyApi {

    Strategy get(String operator);
    Operator operator(String operator);
    List<Action> actionList(String account, String asset, String type, String status);
    Optional<Action> latestAction(String account, String asset, String type, String status);

    List<Action> accept(String sn, INPUT input);

    void save(Action result);
    void save(List<Closed> result);
    void save(Setting setting);
}
