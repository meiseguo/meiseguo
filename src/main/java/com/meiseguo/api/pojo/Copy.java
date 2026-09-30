package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("copy")
@API(value = "复制", remote = true, source = "copy")
public class Copy {

    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "来源配置", visible = true, search = true)
    String setting;

    @API(value = "账户", visible = true, search = true)
    String account;

    @API(value = "操作员", visible = true, search = true)
    String operator;

    @API(value = "创建时间", visible = true, readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();
}
