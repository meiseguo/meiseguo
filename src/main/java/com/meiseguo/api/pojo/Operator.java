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
@Document("operator")
@API(value = "操作员")
public class Operator {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "策略", search = true, visible = true)
    public String strategy;
    @API(value = "策略配置", search = true, visible = true)
    public String setting;
    @API(value = "操作员", search = true, visible = true)
    public String operator;
    @API(value = "开仓数量", visible = true)
    public double openAmt;
    @API(value = "伙伴", search = true)
    public String partner;

    @API(value = "账户", search = true, visible = true)
    public String account;

    @API(value = "币种", search = true, visible = true)
    public String ccy;

    @API(value = "杠杆", visible = true)
    public double lever;

    @API(value = "交易模式", search = true)
    public String tdMode;

    @API(value = "zhang")
    public double zhang = 1000;

    @API(value = "模式", visible = true, type = "case", choice = {"Balance:平衡", "Rescue:自救", "Freeze:冻结", "Clear:清仓"}, search = true)
    public String mode;
    @API(value = "RSI:0/1", choice = {"0:否", "1:是"}, visible = true)
    public int rsi;
    // 止损：对冲+换仓
    @API(value = "止损:0/1", choice = {"0:否", "1:是"}, visible = true)
    public int stopLoss;
    @API(value = "对冲:0/1", choice = {"0:否", "1:是"})
    public int hedge;
    @API(value = "换仓:0/1", choice = {"0:否", "1:是"})
    public int exchange;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
