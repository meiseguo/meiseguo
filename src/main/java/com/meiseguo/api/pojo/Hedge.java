package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("hedge")
@API("对冲")
public class Hedge {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "open", readonly = true)
    String open;
    @API(value = "close", readonly = true)
    String close;
    @API(value = "target", readonly = true)
    String target;
    @API(value = "操作员", search = true, visible = true)
    public String operator;
    //方向: 多 空
    @API(value = "类型", type = "case", choice = {"buy:做多", "sell:做空"})
    public String type;

    @API(value = "方向", type = "case", choice = {"buy:买入", "sell:卖出"}, visible = true)
    public String side;

    // 原价
    @API(value = "价格")
    public double price;

    @API(value = "数量", visible = true)
    public double amount;

    @API(value = "对冲价格")
    public double deal;

    @API(value = "实现盈利", visible = true)
    public double value;

    @API(value = "状态", type = "case", choice ={"init:创建", "live:委托", "filled:成交", "canceled:撤销", "error:异常", "closed:平仓"}, search = true, visible = true)
    public String status;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Hedge(String operator, Action action, StrategyType type) {
        this.operator = operator;
        this.type = type.name();
        this.side = type.name();
        this.target = action.sn.toString();
        this.amount = action.amount;
        this.status = ActionStatus.init.name();
    }

    public Hedge() {
    }
}
