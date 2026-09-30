package com.meiseguo.api.pojo;


import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("invest")
@API("投入")
public class Invest {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "类型", type = "case", choice = {"all:总共", "day:每日", "time:实时"}, visible = true, search = true)
    public String type;

    // 名称
    @API(value = "名称", visible = true, search = true)
    public String name;

    // 最大投入
    @API(value = "最大投入", visible = true)
    public double maxValue;

    // 最大亏损
    @API(value = "最大亏损", visible = true)
    public double minValue;

    @API(value = "在途数量", visible = true)
    public double maxAmount;

    @API(value = "浮亏数量", visible = true)
    public double minAmount;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Invest() {}
    public Invest(String type, String name) {
        this.type = type;
        this.name = name;
        this.maxValue = Double.MIN_VALUE;
        this.minValue = Double.MAX_VALUE;
        this.maxAmount = Long.MIN_VALUE;
        this.minAmount = Long.MAX_VALUE;
    }
}
