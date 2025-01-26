package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 钱包登录前保存nonce，下一步验证。
 */
@Data
@Accessors(chain = true)
@Document("signin")
@API(value = "钱包登录")
public class Signin {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "access_token", visible = true, search = true)
    String access_token;

    @API(value = "address", visible = true, search = true)
    String address;

    @API(value = "nonce", visible = true)
    String nonce;

    @API(value = "过期时间")
    long expireAt;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
