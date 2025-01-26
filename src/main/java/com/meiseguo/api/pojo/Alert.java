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
@Document("alert")
@API(value = "警告")
public class Alert {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "警告", search = true, visible = true)
    public String message;

    @API(value = "触发原因", search = true, visible = true)
    public String trigger;

    @API(value = "操作员", search = true, visible = true)
    public String operator;

    @API(value = "账户", search = true, visible = true)
    public String account;

    @API(value = "币种", search = true, visible = true)
    public String ccy;

    @API(value = "模式", search = true, visible = true)
    public String mode;

    @API(value = "策略", search = true, visible = true)
    public String strategy;

    @API(value = "策略配置", search = true, visible = true)
    public String setting;

    @API(value = "价格", visible = true)
    public double price;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Alert() {}
    public Alert(Operator operator) {
        this.ccy = operator.ccy;
        this.setting = operator.setting;
        this.account = operator.account;
        this.strategy = operator.strategy;
        this.mode = operator.mode;
        this.operator = operator.operator;
    }
}
