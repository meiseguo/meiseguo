package com.meiseguo.api.ctrl;

import com.meiseguo.api.API;
import com.meiseguo.api.pojo.Head;
import com.meiseguo.api.pojo.Reply;
import com.meiseguo.api.pojo.Z;
import com.meiseguo.api.utils.PagesUtil;
import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.meiseguo.api.service.IManageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class InternalController {
    Logger logger = LogManager.getLogger(this.getClass().getName());
    @Autowired
    private MongoTemplate mongoTemplate;

    private static Map<String, IManageService> remote = new HashMap<>();

    @PostConstruct
    @RequestMapping(value = "/internal/init", method = RequestMethod.GET)
    public void init() {
        logger.info("PagesUtil.build(Head)");
        for(Class<?> clazz : PagesUtil.pojos) {
            mongoTemplate.save(PagesUtil.build(clazz));
        }

        logger.info("PagesUtil.build(Head) finish");
    }

    @RequestMapping(value = "/internal/{head}/insert", method = RequestMethod.POST)
    public Reply insert(@PathVariable String head, @RequestBody Map<String, Object> data) {
        logger.info("insert {} {}", head, data);
        Class<?> clazz = PagesUtil.getClass(head);
        if(clazz == null) {
            return Reply.fail("wrong head");
        }
        API api = clazz.getDeclaredAnnotation(API.class);
        Object object = Z.get(data, clazz);
        logger.info("insert {} {}", clazz, object);
        //TODO remote pojo
        if(api.remote()) {
            IManageService service = remote.get(api.source());
            if(service == null) return Reply.fail("no service available");
            return service.insert(clazz, api, object);
        }
        mongoTemplate.save(object);
        Query query = new Query();
        long count = mongoTemplate.count(query, clazz);
        return Reply.success(count);
    }

    @RequestMapping(value = "/internal/{head}/delete/{sn}", method = RequestMethod.POST)
    public Reply delete(@PathVariable String head, @PathVariable String sn) {
        logger.info("delete {} {}", head, sn);
        Query query = new Query(Criteria.where("sn").is(new ObjectId(sn)));
        Class<?> clazz = PagesUtil.getClass(head);
        if(clazz == null) {
            return Reply.fail("wrong head");
        }
        API api = clazz.getDeclaredAnnotation(API.class);
        //TODO remote pojo
        if(api.remote()) {
            IManageService service = remote.get(api.source());
            if(service == null) return Reply.fail("no service available");
            return service.delete(clazz, api, sn);
        }
        DeleteResult result = mongoTemplate.remove(query, clazz);
        return result.getDeletedCount()>0?Reply.success(result.getDeletedCount()):Reply.fail("fail to remove");
    }

    @RequestMapping(value = "/internal/{head}/page", method = RequestMethod.GET)
    public Reply page(@PathVariable String head, @ModelAttribute PageDto dto) {
        logger.info("page {} {}", head, dto);
        Class<?> clazz = PagesUtil.getClass(head);
        if(clazz == null) {
            return Reply.fail("wrong head");
        }

        API api = clazz.getDeclaredAnnotation(API.class);
        //TODO remote pojo
        if(api.remote()) {
            IManageService service = remote.get(api.source());
            if(service == null) return Reply.fail("no service available");
            return service.page(clazz, api, dto);
        }
        Criteria criteria = new Criteria();
        Query query = new Query(criteria).with(Sort.by("sn"));
        if(!ObjectUtils.isEmpty(dto.getLastId())) {
            logger.info("load more from " + dto.getLastId());
            criteria.andOperator(Criteria.where("sn").gt(new ObjectId(dto.getLastId())));
        }
        if(!ObjectUtils.isEmpty(dto.getSearch())) {
            String search = dto.getSearch();
            logger.info("searching for " + search);
            criteria.andOperator(PagesUtil.search(clazz, search));
        }
        if(dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
        long count = mongoTemplate.count(query, clazz);
        query.limit(dto.getPageSize());
        List<?> pojos = mongoTemplate.find(query, clazz);
        return Reply.success(pojos).total(count);
    }


    @RequestMapping(value = "/internal/pages/heads", method = RequestMethod.GET)
    public Reply heads() {
        return Reply.success(PagesUtil.heads).total(PagesUtil.heads.size());
    }

    @RequestMapping(value = "/internal/pages/head/{head}", method = RequestMethod.GET)
    public Reply head(@PathVariable String head) {
        logger.info("get head {}", head);
        Head byId = mongoTemplate.findById(head, Head.class);
        return Reply.success(byId);
    }

    @RequestMapping(value = "/internal/pages/update/{head}", method = RequestMethod.POST)
    public Reply update(@PathVariable String head, @RequestBody UpdateDto dto) {
        Class<?> clazz = PagesUtil.getClass(head);
        if(clazz == null) {
            return Reply.fail("wrong head");
        }
        //bugfix: readonly
        API title = PagesUtil.getByTitle(dto.getTitle(), clazz);
        if(title == null || title.readonly()) {
            logger.error("try to change a readonly attr {}", dto.getTitle());
            return Reply.fail("readonly");
        }

        API api = (API) clazz.getDeclaredAnnotation(API.class);
        //TODO remote pojo
        if(api.remote()) {
            IManageService service = remote.get(api.source());
            if(service == null) return Reply.fail("no service available");
            return service.update(clazz, api, dto);
        }

        Query query = new Query(Criteria.where("sn").is(new ObjectId(dto.getSn())));
        Update update = Update.update(dto.getTitle(), dto.getNewVal());
        UpdateResult upsert = mongoTemplate.updateFirst(query, update, clazz);
        try {
            Update updatetime = Update.update("updatetime", LocalDateTime.now());
            mongoTemplate.updateFirst(query, updatetime, clazz);
        } catch (Exception e) {
            logger.warn("fail to update updatetime for {}", clazz);
        }
        logger.info("update result: {}, {}", dto, upsert);
        return Reply.success(upsert.getModifiedCount());
    }
}
