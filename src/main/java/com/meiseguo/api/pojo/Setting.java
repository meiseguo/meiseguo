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
@Document("setting")
@API(value = "策略配置")
public class Setting {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();


    /**
     * 策略唯一Key
     */
    @API(value = "策略", search = true, visible = true)
    public String strategy;
    @API(value = "配置", search = true, visible = true)
    public String setting;
    @API(value = "备注", search = true)
    public String remark;
    @API(value = "黄金比例")
    public double goldenRatio = 1.618;
    /**
     * 标准单笔大小
     */
    @API(value = "最小单位", visible = true)
    public double unitAmt;
    @API(value = "开仓数量", visible = true)
    public double openAmt;
    /**
     * RSI范围
     */
    @API(value = "最小RSI", visible = true)
    public double minRsi;
    @API(value = "最大RSI", visible = true)
    public double maxRsi;


    /**
     * 超过时间差允许投资
     */
    @API(value = "补仓：秒")
    public long timeGap;
    @API(value = "减仓：秒")
    public long timeGapClose;
    @API(value = "最快：秒")
    public long timeGapWin;
    @API(value = "最慢：秒")
    public long timeGapLoss;

    /**
     * 滑点
     */
    @API(value = "滑点0.001", visible = true)
    public double slippage;
    @API(value = "最小振幅", visible = true)
    public double priceDiffMin;

    /**
     * 盈利率：2%
     */
    @API(value = "止盈比例", visible = true)
    public double winRatio;
    @API(value = "不亏比例", visible = true)
    public double minRatio;

    @API(value = "-止损比例", visible = true)
    public double stopLossRatio;
    @API(value = "-回撤比例", visible = true)
    public double drawdownRatio;

    /**
     * 限额，超过就不能加仓了。清仓之后这个又重新计算
     */
    @API(value = "限额", visible = true)
    public double limitedValue;

    /**
     * 限损，超过这么大损失就要止损一笔
     */
    @API(value = "限损", visible = true)
    public double limitedLoss;
    /**
     * 限次：7天内相同价位不允许投资超过这么多次
     */
    @API(value = "限次", visible = true)
    public long limitedCount;
    /**
     * 时间限制：短线必须要在固定时间内完成减仓
     */
    @API(value = "限时：毫秒", visible = true)
    public long limitedTime;
    public String getLimitedTime() {
        return TimeUnit.MILLISECONDS.toMinutes(limitedTime) + "分钟";
    }

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "均线止盈", type = "case", choice = {"0:不要求", "1:均线内"})
    public int average = 0;

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
