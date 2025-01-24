package com.meiseguo.api.service;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;
import com.meiseguo.api.strategy.INPUT;
import com.meiseguo.api.strategy.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StrategyService implements StrategyApi {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Action> get_tickers(String operator, double price) {
        // TODO
        return new ArrayList<>();
    }

    @Override
    public Strategy get(String operator) {
        return null;
    }

    @Override
    public Operator operator(String operator) {
        return null;
    }

    @Override
    public List<Action> actionList(String account, String asset, String type, String status) {
        return new ArrayList<>();
    }

    @Override
    public Optional<Action> latestAction(String account, String asset, String type, String status) {
        return Optional.empty();
    }

    @Override
    public List<Action> accept(String sn, INPUT input) {
        return new ArrayList<>();
    }

    @Override
    public void save(Action action) {

    }

    @Override
    public void save(List<Closed> result) {

    }

    @Override
    public void save(Setting setting) {

    }
}
