package com.meiseguo.api.service;

import com.meiseguo.api.API;
import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.meiseguo.api.pojo.Online;
import com.meiseguo.api.pojo.Record;
import com.meiseguo.api.pojo.Reply;
import com.meiseguo.api.pojo.StrategyType;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class OnlineManageService implements IManageService {

    @Override
    public Reply page(Class<?> clazz, API api, PageDto dto) {
        Map<String, Record> records = StrategyService.records;
        List<Online> list = records.entrySet().stream().map(entry -> {
            Record record = entry.getValue();
            Online online = new Online();
            online.setAverage(record.average(1));
            online.setDown(record.average(-1));
            online.setSize(record.index.get());
            online.setDuration(record.duration);
            online.setBuy(record.nextTimes(StrategyType.buy));
            online.setSell(record.nextTimes(StrategyType.sell));
            online.setCcy(entry.getKey());
            if(record.current != null) {
                online.setMillis(Instant.ofEpochMilli(record.current.millis).atZone(ZoneId.systemDefault()).toLocalDateTime());
                online.setPrice(record.current.price);
            }
            online.setLatest(record.latest());
            online.setDecline(record.decline());
            online.setTimeLeft(record.timeLeft());
            return online;
        }).collect(Collectors.toList());
        return Reply.success(list).total(records.size());
    }

    @Override
    public Reply update(Class<?> clazz, API api, UpdateDto dto) {
        return Reply.fail();
    }

    @Override
    public Reply insert(Class<?> clazz, API api, Object object) {
        return Reply.fail();
    }

    @Override
    public Reply delete(Class<?> clazz, API api, String sn) {
        return Reply.fail();
    }
}
