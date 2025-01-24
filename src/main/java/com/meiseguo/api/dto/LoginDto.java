package com.meiseguo.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDto {
    @NotBlank(message = "用户名必填")
    String username;
    @NotBlank(message = "密码必填")
    String password;
}
