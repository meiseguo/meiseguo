package com.meiseguo.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangeDto {
    @NotBlank(message = "旧密码必填")
    String oldPassword;
    @NotBlank(message = "新密码必填")
    String password;
}
