package com.meiseguo.api.ctrl;

import com.meiseguo.api.pojo.Reply;
import com.meiseguo.api.service.StrategyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class ApiController {
    Logger logger = LogManager.getLogger(this.getClass().getName());

    @Autowired
    private StrategyService service;

    @PostMapping("/actions/{operator}/{price}")
    public Reply actions(@PathVariable String operator, @PathVariable double price) {
        return Reply.success(service.actions(operator, price));
    }

    @PostMapping("/update/{ordId}/{sn}/{status}")
    public Reply login(@PathVariable String ordId, @PathVariable String sn, @PathVariable String status) {
        service.update(ordId, sn, status);
        return Reply.success();
    }

}
