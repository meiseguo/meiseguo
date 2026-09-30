package com.meiseguo.api.service;

import com.meiseguo.api.API;
import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.meiseguo.api.pojo.Relax;
import com.meiseguo.api.pojo.Reply;
import com.meiseguo.api.pojo.Z;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
public class RelaxManageService implements IManageService {

    @Override
    public Reply page(Class<?> clazz, API api, PageDto dto) {
        Map<String, Relax> relaxMap = StrategyService.relaxMap;
        return Reply.success(relaxMap.values()).total(relaxMap.size());
    }

    @Override
    public Reply update(Class<?> clazz, API api, UpdateDto dto) {
        StrategyService.relaxMap.values().stream().filter(relax -> dto.getSn().equals(relax.getSn().toHexString())).findFirst().ifPresent(relax -> {
            Z.set(dto.getTitle(), relax, dto.getNewVal());
            relax.setUpdatetime(LocalDateTime.now());
        });
        return Reply.success();
    }

    @Override
    public Reply insert(Class<?> clazz, API api, Object object) {
        Relax relax = (Relax) object;
        StrategyService.relaxMap.put(relax.getOperator() + "." + relax.getReason(), relax);
        return Reply.fail();
    }

    @Override
    public Reply delete(Class<?> clazz, API api, String sn) {
        StrategyService.relaxMap.forEach((key, value) -> {
            if (value.getSn().toHexString().equals(sn)) {
                StrategyService.relaxMap.remove(key);
            }
        });
        return Reply.success();
    }
}
