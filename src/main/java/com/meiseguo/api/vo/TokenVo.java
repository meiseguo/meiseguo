package com.meiseguo.api.vo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * token 接口访问需要验证这个。
 */
@Data
@Accessors(chain = true)
public class TokenVo {
    @API(value = "token", visible = true)
    String token;

    @API(value = "过期时间")
    long expireAt;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "最新版本")
    String version;

    @API(value = "新版描述")
    String desc;

    @API(value = "English")
    String en_desc;

    @API(value = "新版地址")
    String link;

    @API(value = "apk下载地址")
    String apkUrl;

//    public static void main(String[] args) {
//        BigDecimal totalPrep = BigDecimal.valueOf(3.13);
//        BigDecimal totalCase = BigDecimal.valueOf(3);
//        BigDecimal times = totalPrep.divide(totalCase, 0, RoundingMode.FLOOR);
//        System.out.println(times.toPlainString());
//        BigDecimal result = times.multiply(totalCase);
//        System.out.println(result.toPlainString());
//    }
}
