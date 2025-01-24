package com.meiseguo.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PublishDto {

    /**
     * 标题
     */
    @NotBlank
    String title;

    /**
     * 需求描述
     */
    @NotBlank
    String description;

}
