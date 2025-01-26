package com.meiseguo.api.pojo;


import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("closed")
@API("平仓记录")
public class Closed {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "open", readonly = true)
    ObjectId open = new ObjectId();
    @API(value = "close", readonly = true)
    ObjectId close = new ObjectId();

    @API(value = "ccy", search = true, visible = true)
    public String ccy;

    //方向: 多 空
    @API(value = "类型", type = "case", choice = {"buy:做多", "sell:做空"}, visible = true)
    public String type;

    // 均价
    @API(value = "价格", visible = true)
    public double price;

    @API(value = "已平仓数量", visible = true)
    public double closed;

    @API(value = "平仓价格", visible = true)
    public double deal;

    @API(value = "手续费", visible = true)
    public double fee;

    @API(value = "状态", type = "case", choice = {"init:创建", "pending:委托", "deal:成交", "cancel:撤销"}, search = true, visible = true)
    public String status;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
