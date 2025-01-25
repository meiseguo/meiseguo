package com.meiseguo.api.strategy;

import com.meiseguo.api.StrategyApi;
import com.meiseguo.api.pojo.*;
import com.meiseguo.api.service.StrategyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class Strategy implements Function<INPUT, List<Action>> {
    public String buy = "buy";
    public String sell = "sell";
    public String deal = "deal";
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
        // 安全吗？
        if(safe(input.getPrice())) {
            // 安全系数是多少？
            // 如果小于需要停止吗？
            double weight = margin(input.getPrice());
            if(weight < setting.threshold) {
                // 什么也不做会更好吗？
                System.out.println("危险区");
            } else {
                // 足够安全那么才投资
                // 可以加仓吗？时间跨度满足条件了吗？价格差满足条件了吗？最近一次价格差够盈利吗？
                // 不同的策略不同的操作？
                buy(input).ifPresent(result::add);
                sell(input).ifPresent(result::add);
            }
        } else {
            // 不安全需要切换到Rescue模式吗？
            // api.alert(safety, input);
            System.out.println("不安全");
        }
        return result;
    }

    public abstract boolean safe(double price);
    public abstract double margin(double price);
    public abstract Optional<Action> sell(INPUT input);
    public abstract Optional<Action> buy(INPUT input);
}