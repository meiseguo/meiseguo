package com.meiseguo.api.service;

import com.meiseguo.api.pojo.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Service
public class ConfigService {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private MongoTemplate mongoTemplate;

    public Optional<String> get(String key) {
        Config config = mongoTemplate.findOne(new Query(Criteria.where("key").is(key)), Config.class);
        logger.info("[config] {} = {}", key, config);
        if(ObjectUtils.isEmpty(config)) {
            return Optional.empty();
        }
        return Optional.of(config.getValue());
    }
}
