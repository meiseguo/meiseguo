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
@Document("account")
@API(value = "账户")
public class Account {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "账户", search = true, visible = true)
    public String account;

    @API(value = "ccy", search = true, visible = true)
    public String ccy;

    @API(value = "类型", type = "case", choise = {"main:主号", "sub:字号"}, visible = true)
    public String type;

    @API(value = "余额", visible = true)
    public double balance;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choise = {"0:正常", "1:已删除"})
    int deleted = 0;
}
