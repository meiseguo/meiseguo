package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("round")
@API("开一局")
public class Round {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "操作员", search = true, visible = true)
    public String operator;

    @API(value = "计数", visible = true)
    public String round;

    @API(value = "单笔数量", visible = true)
    public double unit = 1000;

    @API(value = "ccy", search = true, readonly = true)
    public String ccy;
    //方向: 多 空
    @API(value = "类型", type = "case", choice = {"buy:做多", "sell:做空"}, readonly = true, visible = true)
    public String type;

    @API(value = "总数量", visible = true)
    public double amount = 0.0;

    @API(value = "状态", type = "case", visible = true, choice ={"init:创建", "live:委托", "filled:成交", "canceled:撤销", "error:异常", "closed:平仓"}, search = true)
    public String status;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
