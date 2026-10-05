package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import com.meiseguo.api.strategy.Input;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Data
@Accessors(chain = true)
@Document("pending")
@API(value = "在途订单", remote = true, source = "pending")
public class Pending {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "订单号", search = true)
    public String order;

    @API(value = "当前价", visible = true)
    public double current;

    @API(value = "跟单:0/1", choice = {"0:否", "1:是"})
    public int follow;

    @API(value = "方向", type = "case", choice = {"buy:买入", "sell:卖出"}, visible = true)
    public String side;

    @API(value = "价格", visible = true)
    public double price;

    @API(value = "涨跌幅", visible = true)
    public String ratio;

    @API(value = "回撤率", visible = true)
    public String drawdown;

    @API(value = "时间差", visible = true)
    public String gap;

    @API(value = "账户", search = true)
    public String account;

    @API(value = "策略配置", search = true)
    public String setting;

    @API(value = "分支理由", search = true)
    public String reason;

    @API(value = "状态", type = "case", choice = {"init:创建", "live:委托", "filled:成交", "canceled:撤销", "error:异常", "closed:平仓"}, search = true, visible = true)
    public String status;

    @API(value = "ccy", search = true)
    public String ccy;

    @API(value = "模式", search = true)
    public String mode;

    @API(value = "交易模式", search = true)
    public String tdMode;

    @API(value = "zhang")
    public double zhang;

    @API(value = "策略", search = true)
    public String strategy;

    @API(value = "操作员", search = true, visible = true)
    public String operator;

    //方向: 多 空
    @API(value = "类型", type = "case", choice = {"buy:做多", "sell:做空"})
    public String type;

    @API(value = "数量", visible = true)
    public double amount;

    @API(value = "金额")
    public double value;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Pending() {}
    public Pending(Action action, Input current) {
        this.operator = action.operator;
        this.setting = action.setting;
        this.strategy = action.strategy;
        this.account = action.account;
        this.tdMode = action.tdMode;
        this.zhang = action.zhang;
        this.mode = action.mode;
        this.ccy = action.ccy;
        this.type = action.type;
        this.status = action.status;
        this.price = action.price;
        this.side = action.side;
        this.amount = action.amount;
        this.value = action.value;
        this.reason = action.reason;
        this.follow = action.follow;
        this.createtime = action.createtime;
        this.current = current.price;
        this.ratio = String.format("%.2f%%", (100.0 * action.winRatio(current)));
        this.drawdown = String.format("%.2f%%", (100.0 * action.turnRatio(current)));
        this.gap = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - action.millis) + "分钟";
    }
}
