package com.meiseguo.api.dto;

import lombok.Data;

/**
 * 获取token
 */
@Data
public class TokenDto {
    String sign;
    String appid;
    String random;
}
