package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import com.meiseguo.api.strategy.Input;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

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

    @API(value = "开始价格")
    public double from;

    @API(value = "结束价格")
    public double to;

    @API(value = "价格差")
    public double diff;

    @API(value = "振幅", visible = true)
    public String ratio;

    public String getRatio() {
        return String.format("%.2f%%", (100.0 * diff / from));
    }

    @API(value = "时间差", readonly = true, type = "time", visible = true)
    public long gap;
    public String getGap() {
        return TimeUnit.MILLISECONDS.toMinutes(gap) + "分钟";
    }

    @API(value = "开始", readonly = true, type = "time")
    public LocalDateTime start;

    @API(value = "结束", readonly = true, type = "time")
    public LocalDateTime end;

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Statistic(){}
    public Statistic(Input input, Input output) {
        this.start = Instant.ofEpochMilli(input.millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.end = Instant.ofEpochMilli(output.millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.ccy = input.ccy;
        this.from = input.price;
        this.to = output.price;
        this.gap = output.millis - input.millis;
        this.diff = output.price - input.price;
    }
}
