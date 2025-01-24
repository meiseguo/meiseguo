package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("config")
@API("[系统配置]")
public class Config {

    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @Id
    @API(value = "key", visible = true, search = true)
    String key;

    @API(value = "value", visible = true, search = true)
    String value;

    @API(value = "内容", visible = true, search = true)
    String message;

    @API(value = "创建时间", visible = true, readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    public String part(String value) {
        if(value == null) return "null";
        if(value.length() > 10) return value.substring(0, 10) + "...[" + value.length() + "]";
        return value;
    }

    @Override
    public String toString() {
        return "Config{" + key + "=" + part(value) + '}';
    }
}
