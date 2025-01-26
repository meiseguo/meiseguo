package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document("credits")
@API("评分")
public class Credits {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "address", readonly = true, visible = true, search = true)
    String address;

    //card.sn | openid | appid
    @API(value = "ref_id", readonly = true, search = true)
    String ref_id;

    @API(value = "原因", readonly = true, visible = true, search = true)
    String reason;

    @API(value = "积分", readonly = true, visible = true)
    BigDecimal credits;

    @API(value = "类型", type = "case", choice = {"bonus:邀请奖励", "dynamic:动态收益","transfer:转账", "statement:日结","consume:消耗","gain:收益", "charge:充值", "refund:退款","buy:交易","withdraw:提现", "sale:寄售", "bid:竞标","fee:手续费"}, visible = true)
    String type = "";

    @API(value = "状态", type = "case", choice = {"INIT:初始化", "APPLY:提现", "PROCESS:处理", "DONE:已发放"}, visible = true)
    String status = "INIT";

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", readonly = true, type = "time")
    LocalDateTime updatetime = LocalDateTime.now();
}