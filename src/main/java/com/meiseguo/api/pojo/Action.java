package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import com.meiseguo.api.strategy.Input;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Document("action")
@API(value = "下单记录")
public class Action {
    @Id
    @API(value = "sn", readonly = true, search = true)
    ObjectId sn = new ObjectId();

    @API(value = "订单号", search = true)
    public String order;

    @API(value = "账户", search = true)
    public String account;

    @API(value = "策略配置", search = true)
    public String setting;

    @API(value = "分支理由", search = true)
    public String reason;

    @API(value = "状态", type = "case", choice = {"init:创建", "live:委托", "filled:成交", "cancel:请撤销", "canceled:撤销", "error:异常", "closing:请平仓", "closed:平仓"}, search = true, visible = true)
    public String status;

    @API(value = "方向", type = "case", choice = {"buy:买入", "sell:卖出"}, visible = true)
    public String side;

    @API(value = "价格", visible = true)
    public double price;

    @API(value = "标记价格", visible = true)
    public double mark;

    @API(value = "极限价格", visible = true)
    public double minmax;

    @API(value = "止赢:0/1", type = "case", choice = {"0:否", "1:是"}, visible = true)
    public int stopWin;

    @API(value = "止损:0/1", type = "case", choice = {"0:否", "1:是"}, visible = true)
    public int stopLoss;

    @API(value = "ccy", search = true)
    public String ccy;

    @API(value = "模式", type = "case", choice = {"Balance:平衡", "Clear:清仓", "Rescue:自救", "Freeze:冻结"}, search = true)
    public String mode;

    @API(value = "交易模式")
    public String tdMode;

    @API(value = "zhang")
    public double zhang;

    @API(value = "策略")
    public String strategy;

    @API(value = "跟单:0/1", type = "case", choice = {"0:否", "1:是"})
    public int follow;

    @API(value = "操作员", search = true, visible = true)
    public String operator;

    //方向: 多 空
    @API(value = "类型", type = "case", choice = {"buy:做多", "sell:做空"})
    public String type;

    @API(value = "数量", visible = true)
    public double amount;

    @API(value = "金额", visible = true)
    public double value;

    @API(value = "已平仓数量")
    public double closed;

    @API(value = "手续费")
    public double fee;

    @API(value = "计时")
    public long millis;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Action() {}
    public Action(Operator operator, StrategyType type) {
        this.operator = operator.operator;
        this.setting = operator.setting;
        this.strategy = operator.strategy;
        this.account = operator.account;
        this.tdMode = operator.tdMode;
        this.zhang = operator.zhang;
        this.mode = operator.mode;
        this.ccy = operator.ccy;
        this.type = type.name();
        this.status = ActionStatus.init.name();
    }

    public void sell(Input input, double amount) {
        this.side = OrderSide.sell.name();
        this.price = input.price;
        this.minmax = price;
        this.mark = price;
        this.amount = amount;
        this.value = amount * price;
        this.millis = input.millis;
    }

    public void buy(Input input, double amount) {
        this.side = OrderSide.buy.name();
        this.price = input.price;
        this.minmax = price;
        this.mark = price;
        this.amount = amount;
        this.value = amount * price;
        this.millis = input.millis;
    }

    public double winRatio(Input input) {
        if(StrategyType.buy.name().equals(type)) {
            return (input.price - this.price) / this.price;
        }

        if(StrategyType.sell.name().equals(type)) {
            return (this.price - input.price) / this.price;
        }
        return 0;
    }

    public double turnRatio(Input input) {
        if(StrategyType.buy.name().equals(type)) {
            return (input.price - this.minmax) / input.price;
        }

        if(StrategyType.sell.name().equals(type)) {
            return (this.minmax - input.price) / input.price;
        }
        return 0;
    }

    public Closed closedBy(Action close) {
        Action open = this;
        double amt = Math.min(open.amount, close.amount);
        close.setMark(open.mark);
        close.setMinmax(open.minmax);
        close.setStopLoss(open.stopLoss);
        close.setStopWin(open.stopWin);
        open.setStatus(ActionStatus.closed.name());
        open.setUpdatetime(LocalDateTime.now());
        open.setClosed(amt);
        Closed closed = new Closed();
        closed.setOpen(open.sn);
        closed.setClose(close.sn);
        closed.setOperator(close.operator);
        closed.setCcy(open.ccy);
        closed.setType(open.type);
        closed.setPrice(open.price);
        closed.setClosed(amt);
        closed.setDeal(close.price);
        closed.setStatus(open.status);
        closed.setCreatetime(open.createtime);
        closed.setUpdatetime(open.updatetime);
        if (StrategyType.buy.name().equals(open.type)) {
            closed.setValue(open.amount * (close.price - open.price));
            closed.setGain(open.amount - close.amount);
            closed.setGainType("SPOT");
        } else {
            closed.setValue(open.amount * (open.price - close.price));
            closed.setGain(open.value - close.value);
            closed.setGainType("USDT");
        }
        return closed;
    }
}
