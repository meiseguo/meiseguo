package com.meiseguo.api.ctrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;

/**
 * 无门槛，无权限
 */
@Controller
@CrossOrigin
@RequestMapping("/server")
public class ServerController {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @PostConstruct
    public void init() {
        logger.info("Open controller started");
    }

    @GetMapping(value = "/boss")
    public String boss(){
        return "boss";
    }
    @GetMapping(value = "/index")
    public String index(){
        return "index";
    }
    @GetMapping(value = "/pages")
    public String pages(){
        return "pages";
    }

}