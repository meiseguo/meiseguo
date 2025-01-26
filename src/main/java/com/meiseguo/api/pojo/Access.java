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
 * access 客户登录之后才会有access，有了这个才算授权通过。
 */
@Data
@Accessors(chain = true)
@Document("access")
@API("权限")
public class Access {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "username", visible = true, search = true)
    @Dict(Z.openid)
    String username;

    @API(value = "access", visible = true, search = true)
    String access;

    @API(value = "过期时间", visible = true)
    long expireAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(20);

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
