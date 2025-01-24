package com.meiseguo.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateDto {
    @NotBlank
    String sn;
    @NotBlank
    String title;

    String oldVal;
    @NotBlank
    String newVal;
}
