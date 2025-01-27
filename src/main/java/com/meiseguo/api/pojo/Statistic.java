package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import com.meiseguo.api.strategy.INPUT;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Accessors(chain = true)
@Document("statistic")
@API(value = "统计波动")
public class Statistic {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "币种", search = true, visible = true)
    public String ccy;

    @API(value = "开始价格", visible = true)
    public double from;

    @API(value = "结束价格", visible = true)
    public double to;

    @API(value = "价格差", visible = true)
    public double diff;

    @API(value = "时间差", readonly = true, type = "time", visible = true)
    public long gap;

    @API(value = "开始", readonly = true, type = "time", visible = true)
    public LocalDateTime start;

    @API(value = "结束", readonly = true, type = "time", visible = true)
    public LocalDateTime end;

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Statistic(){}
    public Statistic(INPUT start, INPUT end) {
        this.start = Instant.ofEpochMilli(start.millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.end = Instant.ofEpochMilli(end.millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.from = start.price;
        this.to = end.price;
        this.gap = end.millis - start.millis;
    }
}
