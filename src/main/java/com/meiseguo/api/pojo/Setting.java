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
@Document("setting")
@API(value = "策略配置")
public class Setting {
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
    @API(value = "备注", visible = true, search = true)
    public String remark;
    @API(value = "黄金比例")
    public double goldenRatio = 1.618;
    /**
     * 标准单笔大小
     */
    @API(value = "单笔买入数量", visible = true)
    public double unitBuyAmt;
    @API(value = "单笔卖出数量", visible = true)
    public double unitSellAmt;

    /**
     * 超过时间差允许投资
     */
    @API(value = "下单时间跨度", visible = true)
    public long timeGap;
    @API(value = "至少时间跨度", visible = true)
    public long timeGapMin;

    /**
     * 超过价格差允许投资
     */
    @API(value = "最大涨跌幅", visible = true)
    public double priceDiff;
    @API(value = "最小涨跌幅", visible = true)
    public double priceDiffMin;

    /**
     * 盈利率：2%
     */
    @API(value = "单笔止盈比率", visible = true)
    public double winRatio;
    @API(value = "最小止盈比例", visible = true)
    public double minRatio;

    /**
     * 限额，超过就不能加仓了。清仓之后这个又重新计算
     */
    @API(value = "投资限额", visible = true)
    public double limitedValue;


    /**
     * 阈值：可以决定下注的大小
     */
    @API(value = "安全边界阈值", visible = true)
    public double threshold;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choise = {"0:正常", "1:已删除"})
    int deleted = 0;
}
