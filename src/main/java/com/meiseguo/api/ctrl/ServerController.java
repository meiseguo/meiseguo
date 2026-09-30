package com.meiseguo.api.ctrl;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

/**
 * 无门槛，无权限
 */
@Controller
@CrossOrigin
@RequestMapping("/server")
public class ServerController {
    Logger logger = LogManager.getLogger(this.getClass().getName());

    @PostConstruct
    public void init() {
        logger.info("server started");
    }

    @GetMapping(value = "/boss")
    public String boss(){
        return "boss";
    }
    @GetMapping(value = "/data")
    public String data(){
        return "data";
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