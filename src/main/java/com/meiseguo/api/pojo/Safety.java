package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Document("safety")
@API(value = "⚠️安全 ⚠️")
public class Safety {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    /**
     * 策略唯一Key
     */
    @API(value = "策略", visible = true, search = true)
    public String strategy;
    @API(value = "策略配置", search = true, visible = true)
    public String setting;
    // 哪一种
    @API(value = "币种", visible = true)
    public String ccy;

    //方向: 多 空
    @API(value = "类型", type = "case", choice = {"buy:做多", "sell:做空"}, visible = true)
    public String type;

    // 周期：1分钟，15分钟，1小时
    @API(value = "周期:ms", visible = true)
    public long interval;

    // 天花板
    @API(value = "天花板", visible = true)
    public double ceiling;

    // 均价
    @API(value = "均价", visible = true)
    public double average;

    // 安全边界（生命线-----最大价差）
    @API(value = "安全边界", visible = true)
    public double safety;

    // 生命线（预测）
    @API(value = "生命线", visible = true)
    public double alive;

    // 地板（设置）
    @API(value = "地板", visible = true)
    public double floor;

    // 危险（可调节）
    @API(value = "危险", visible = true)
    public double danger;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}

