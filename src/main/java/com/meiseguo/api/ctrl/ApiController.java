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

    @PostMapping("/update/{ordid}/{sn}/{status}/{amount}/{price}")
    public Reply login(@PathVariable String ordid, @PathVariable String sn, @PathVariable String status,@PathVariable double amount, @PathVariable double price) {
        service.update(ordid, sn, status, amount, price);
        return Reply.success();
    }

}
