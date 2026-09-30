package com.meiseguo.api.ctrl;

import com.meiseguo.api.API;
import com.meiseguo.api.dto.PageDto;
import com.meiseguo.api.dto.UpdateDto;
import com.meiseguo.api.pojo.Head;
import com.meiseguo.api.pojo.Invest;
import com.meiseguo.api.pojo.Reply;
import com.meiseguo.api.pojo.Z;
import com.meiseguo.api.service.*;
import com.meiseguo.api.utils.PagesUtil;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
public class InternalController {
    Logger logger = LogManager.getLogger(this.getClass().getName());
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StrategyService strategyService;

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private static final Map<String, IManageService> remote = new HashMap<>();

    @PostConstruct
    @RequestMapping(value = "/internal/init", method = RequestMethod.GET)
    public void init() {
        logger.info("PagesUtil.build(Head)");
        for (Class<?> clazz : PagesUtil.classes) {
            mongoTemplate.save(PagesUtil.build(clazz));
        }
        PendingManageService pending = new PendingManageService(mongoTemplate);
        ProcessService proc = new ProcessService(mongoTemplate);
        Map<String, Invest> invest = pending.getInvest();
        remote.put("online", new OnlineManageService());
        remote.put("relax", new RelaxManageService());
        remote.put("pending", pending);
        remote.put("proc", proc);
        remote.put("copy", new CopyManageService(mongoTemplate));
        logger.info("PagesUtil.build(Head) finish, register service ok");
        service.scheduleWithFixedDelay(() -> {
//            boolean delay = StrategyService.records.values().stream().allMatch(record ->
//                    !ObjectUtils.isEmpty(record.current) && System.currentTimeMillis() - record.current.millis > TimeUnit.SECONDS.toMillis(20)
//            );
//            if (delay) {
//                proc.restart();
//            }
            pending.update(invest);
        }, 10, 10, TimeUnit.SECONDS);
    }

    @RequestMapping(value = "/internal/{head}/insert", method = RequestMethod.POST)
    public Reply insert(@PathVariable String head, @RequestBody Map<String, Object> data) {
        Class<?> clazz = PagesUtil.getClass(head);
        if (clazz == null) {
            return Reply.fail("wrong head");
        }
        API api = clazz.getDeclaredAnnotation(API.class);
        Object object = Z.get(data, clazz);
        if (api.remote()) {
            IManageService service = remote.get(api.source());
            if (service == null) return Reply.fail("no service available");
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
        if (clazz == null) {
            return Reply.fail("wrong head");
        }
        API api = clazz.getDeclaredAnnotation(API.class);
        if (api.remote()) {
            IManageService service = remote.get(api.source());
            if (service == null) return Reply.fail("no service available");
            return service.delete(clazz, api, sn);
        }
        DeleteResult result = mongoTemplate.remove(query, clazz);
        return result.getDeletedCount() > 0 ? Reply.success(result.getDeletedCount()) : Reply.fail("fail to remove");
    }

    @RequestMapping(value = "/internal/{head}/page", method = RequestMethod.GET)
    public Reply page(@PathVariable String head, @ModelAttribute PageDto dto) {
        Class<?> clazz = PagesUtil.getClass(head);
        if (clazz == null) {
            return Reply.fail("wrong head");
        }

        API api = clazz.getDeclaredAnnotation(API.class);
        if (api.remote()) {
            IManageService service = remote.get(api.source());
            if (service == null) return Reply.fail("no service available");
            return service.page(clazz, api, dto);
        }
        Criteria criteria = new Criteria();
        Query query = new Query(criteria).with(Sort.by("sn").descending());
        if (!ObjectUtils.isEmpty(dto.getLastId())) {
            logger.info("load more from {}", dto.getLastId());
            criteria.andOperator(Criteria.where("sn").lt(new ObjectId(dto.getLastId())));
        }
        if (!ObjectUtils.isEmpty(dto.getSearch())) {
            String search = dto.getSearch();
            logger.info("searching for {}", search);
            criteria.andOperator(PagesUtil.search(clazz, search));
        }
        if (dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
        long count = mongoTemplate.count(query, clazz);
        query.limit(dto.getPageSize());
        List<?> list = mongoTemplate.find(query, clazz);
        if (api.secret()) {
            list.forEach(PagesUtil::secure);
        }
        return Reply.success(list).total(count);
    }


    @RequestMapping(value = "/internal/pages/heads", method = RequestMethod.GET)
    public Reply heads() {
        return Reply.success(PagesUtil.heads).total(PagesUtil.heads.size());
    }

    @RequestMapping(value = "/internal/pages/head/{head}", method = RequestMethod.GET)
    public Reply head(@PathVariable String head) {
        Head byId = mongoTemplate.findById(head, Head.class);
        return Reply.success(byId);
    }

    @RequestMapping(value = "/internal/pages/update/{head}", method = RequestMethod.POST)
    public Reply update(@PathVariable String head, @RequestBody UpdateDto dto) {
        Class<?> clazz = PagesUtil.getClass(head);
        if (clazz == null) {
            return Reply.fail("wrong head");
        }
        //bugfix: readonly
        API title = PagesUtil.getByTitle(dto.getTitle(), clazz);
        if (title == null || title.readonly()) {
            logger.error("try to change a readonly attr {}", dto.getTitle());
            return Reply.fail("readonly");
        }

        API api = clazz.getDeclaredAnnotation(API.class);
        if (api.remote()) {
            IManageService service = remote.get(api.source());
            if (service == null) return Reply.fail("no service available");
            return service.update(clazz, api, dto);
        }

        Query query = new Query(Criteria.where("sn").is(new ObjectId(dto.getSn())));
        Update update = Update.update(dto.getTitle(), dto.getNewVal());
        UpdateResult upsert = mongoTemplate.updateFirst(query, update, clazz);
        try {
            Update updateTime = Update.update("updatetime", LocalDateTime.now());
            mongoTemplate.updateFirst(query, updateTime, clazz);
        } catch (Exception e) {
            logger.warn("fail to update updateTime for {}", clazz);
        }
        logger.info("update result: {}, {}", dto, upsert);
        return Reply.success(upsert.getModifiedCount());
    }

    @PostMapping("/internal/data/{ccy}")
    public Reply statistic(@PathVariable String ccy) {
        try {
            return Reply.success(strategyService.data(ccy));
        } catch (Exception e) {
            e.printStackTrace();
            return Reply.fail(e.getMessage());
        }
    }
}
