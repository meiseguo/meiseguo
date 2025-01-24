package com.meiseguo.api.vo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * 客户登录之后token放在header，有了这个才授权通过。
 */
@Data
@Accessors(chain = true)
public class LoginVo {
    @API(value = "openid")
    String openid;
    @API(value = "token")
    String token;
    @API(value = "access")
    String access;
    @API(value = "过期时间")
    long expireAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(20);
    @API(value = "服务器时间")
    long serverTime = System.currentTimeMillis();
}
