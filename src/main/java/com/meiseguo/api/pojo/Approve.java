package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document("approve")
@API("审核")
public class Approve {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "类型", visible = true)
    String type;

    @API(value = "审核内容", visible = true, search = true)
    String message;

    @API(value = "审核批注", search = true)
    String remark;

    @API(value = "address", visible = true, search = true)
    String address;

    @API(value = "value", visible = true, readonly = true)
    BigDecimal value = BigDecimal.ZERO;

    @API(value = "ref_id", search = true)
    String ref_id;

    @API(value = "审核状态", visible = true, type = "case", choice = {"INIT:未審核","REJECT:審核失敗","APPROVED:審核成功","ONLINE:審核完成"})
    String status = "INIT";

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
