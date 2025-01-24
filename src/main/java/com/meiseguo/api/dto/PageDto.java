package com.meiseguo.api.dto;

import lombok.Data;

@Data
public class PageDto {
    /**
     * 搜索关键词？
     */
    String search;
    /**
     * 第几页
     */
    int page=0;
    /**
     * 每页大小
     */
    int pageSize = 10;
    /**
     * 注册用户的username
     */
    String creator;

    String lastId;
}
