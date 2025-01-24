package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * token 接口访问需要验证这个。
 */
@Data
@Accessors(chain = true)
@Document("token")
@API("Token")
public class Token {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();
    @API(value = "key", visible = true, search = true)
    String key;
    @API(value = "version")
    String version;
    @API(value = "token", visible = true)
    String token;
    @API(value = "类型", type = "case", choise = {"api:接口访问", "mini:小程序"})
    String type = "api";

    @API(value = "过期时间")
    long expireAt;

    @API(value = "创建时间", visible = true, readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choise = {"0:正常", "1:已删除"})
    int deleted = 0;

    public static void main(String[] args) {
        System.out.println(TimeUnit.HOURS.toMillis(15));
    }
}
