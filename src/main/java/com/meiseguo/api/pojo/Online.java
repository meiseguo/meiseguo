package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Data
@Accessors(chain = true)
@Document("online")
@API(value = "在线", remote = true, source = "online")
public class Online {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "ccy", visible = true, search = true)
    public String ccy;
    // 均价
    @API(value = "价格")
    public double price;

    @API(value = "平均涨幅", visible = true)
    public double average;
    public String getAverage() {
        return String.format("%.2f%%", (100.0 * average));
    }
    @API(value = "平均跌幅", visible = true)
    public double down;
    public String getDown() {
        return String.format("%.2f%%", (-100.0 * down));
    }

    @API(value = "当前涨跌", visible = true)
    public double latest;
    public String getLatest() {
        return String.format("%.2f%%", (100.0 * latest));
    }

    @API(value = "回撤价格", visible = true)
    public double decline;
    public String getDecline() {
        return String.format("%.2f%%", (100.0 * decline));
    }

    @API(value = "tickers")
    public int size;

    @API(value = "连续做空", visible = true)
    public int sell;

    @API(value = "连续做多", visible = true)
    public int buy;

    @API(value = "周期")
    public long duration;
    public String getDuration() {
        return TimeUnit.MILLISECONDS.toMinutes(duration) + "分钟";
    }

    @API(value = "剩余时间")
    public long timeLeft;
    public String getTimeLeft() {
        return TimeUnit.MILLISECONDS.toMinutes(timeLeft) + "分钟" + TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60 + "秒";
    }

    @API(value = "最新")
    public LocalDateTime millis;
}
