package com.meiseguo.api.service;

import com.meiseguo.api.API;
import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.meiseguo.api.pojo.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CopyManageService implements IManageService {
    private MongoTemplate mongoTemplate;

    public CopyManageService() {}
    public CopyManageService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    public static final Map<String, Copy> relaxMap = new ConcurrentHashMap<>();

    @Override
    public Reply page(Class<?> clazz, API api, PageDto dto) {
        return Reply.success(relaxMap.values()).total(relaxMap.size());
    }

    @Override
    public Reply update(Class<?> clazz, API api, UpdateDto dto) {
        return Reply.fail();
    }

    @Override
    public Reply insert(Class<?> clazz, API api, Object object) {
        Copy copy = (Copy) object;
        relaxMap.put(copy.getSn().toHexString(), copy);
        Operator operator = mongoTemplate.findOne(new Query(Criteria.where("setting").is(copy.getSetting())), Operator.class);
        assert operator != null;
        Setting setting = mongoTemplate.findOne(new Query(Criteria.where("setting").is(operator.setting)), Setting.class);
        Safety safety = mongoTemplate.findOne(Query.query(Criteria.where("setting").is(operator.setting)), Safety.class);
        String today = copy.getOperator();
        operator.setSn(new ObjectId());
        operator.setSetting(today);
        operator.setOperator(today);
        operator.setAccount(copy.getAccount());
        operator.setCreatetime(LocalDateTime.now());
        operator.setMode(Mode.Freeze.name());
        assert setting != null;
        setting.setSn(new ObjectId());
        setting.setSetting(today);
        setting.setCreatetime(LocalDateTime.now());
        assert safety != null;
        safety.setSn(new ObjectId());
        safety.setSetting(today);
        safety.setAccount(operator.getAccount());
        safety.setCreatetime(LocalDateTime.now());
        Asset asset = new Asset();
        asset.setAccount(operator.getAccount());
        asset.setOperator(operator.getOperator());
        asset.setCcy(operator.getCcy());
        Status status = new Status();
        status.setOperator(operator.getOperator());
        status.setLimitLossCount(10);
        status.setLimitWinCount(10);
        mongoTemplate.save(operator);
        mongoTemplate.save(setting);
        mongoTemplate.save(safety);
        mongoTemplate.save(asset);
        mongoTemplate.save(status);
        return Reply.success();
    }

    @Override
    public Reply delete(Class<?> clazz, API api, String sn) {
        relaxMap.remove(sn);
        return Reply.success();
    }
}
