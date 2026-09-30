package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("follow")
@API("跟单")
public class Follow {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "操作员", search = true, visible = true)
    public String operator;

    @API(value = "ccy", search = true, readonly = true)
    public String ccy;

    @API(value = "状态", type = "case", visible = true, choice ={"init:创建", "live:委托", "filled:成交", "canceled:撤销", "error:异常", "closed:平仓", "closing:请平仓"}, search = true)
    public String status;

    @API(value = "触发", type = "case", choice = {"now:立即", "fix:指定价格", "low:底部", "high:顶部"}, visible = true)
    public String strategy;
    //方向: 多 空
    @API(value = "类型", type = "case", choice = {"buy:做多", "sell:做空"}, readonly = true, visible = true)
    public String type;

    @API(value = "方向", type = "case", choice = {"buy:买入", "sell:卖出"}, readonly = true)
    public String side;

    @API(value = "数量", visible = true)
    public double amount;

    @API(value = "次数", readonly = true)
    public int index;

    @API(value = "round", readonly = true)
    public String round;

    @API(value = "open", readonly = true)
    public String open;

    @API(value = "close", readonly = true)
    public String close;

    // 原价
    @API(value = "跟单价格", visible = true)
    public double price;

    @API(value = "平仓盈利")
    public double value;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
