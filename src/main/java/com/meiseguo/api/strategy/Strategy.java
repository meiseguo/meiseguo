package com.meiseguo.api.strategy;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class Strategy implements Function<INPUT, List<Action>> {
    public String strategyType;
    public Operator operator;
    public Setting setting;
    public Safety safety;
    public Mode mode;
    public Asset asset;
    public Account account;
    public StrategyApi api;
    public Strategy(Operator operator, Setting setting, Safety safety, Asset asset, Account account, StrategyApi api) {
        this.operator = operator;
        this.setting = setting;
        this.safety = safety;
        this.mode = Mode.valueOf(operator.mode);
        this.asset = asset;
        this.account = account;
        this.api = api;
    }

    /**
     * 上次价格
     */
    public Record record = new Record();

    /**
     * 策略应用
     * @param input 输入
     */
    @Override
    public List<Action> apply(INPUT input) {
        this.record.accept(input);
        List<Action> result = new ArrayList<>();
        if(safe(input.getPrice())) {
            double weight = margin(input.getPrice());
            if(weight < setting.threshold) {
                Alert alert = new Alert(operator);
                alert.setPrice(input.price);
                alert.setMessage("安全系数小于阈值" + setting.threshold + "，不再投资，请注意调整安全边界或者增加保证金。");
                alert.setTrigger("安全系数检查");
                api.save(alert);
            } else {
                buy(input).ifPresent(result::add);
                sell(input).ifPresent(result::add);
            }
        } else {
            Alert alert = new Alert(operator);
            alert.setPrice(input.price);
            alert.setMessage("不安全，不再投资，请注意调整安全边界或者增加保证金。");
            alert.setTrigger("安全性检查");
            api.save(alert);
        }
        return result;
    }

    public abstract boolean safe(double price);
    public abstract double margin(double price);
    public abstract Optional<Action> sell(INPUT input);
    public abstract Optional<Action> buy(INPUT input);
}