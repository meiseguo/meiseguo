package com.meiseguo.api.service;

import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.meiseguo.api.API;
import com.meiseguo.api.pojo.Reply;

public interface IManageService {

    Reply insert(Class<?> clazz, API api, Object object);

    Reply delete(Class<?> clazz, API api, String sn);

    Reply page(Class<?> clazz, API api, PageDto dto);

    Reply update(Class<?> clazz, API api, UpdateDto dto);
}
