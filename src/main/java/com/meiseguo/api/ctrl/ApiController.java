package com.meiseguo.api.ctrl;

import com.meiseguo.api.dto.*;
import com.meiseguo.api.pojo.Reply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin
@RestController
public class ApiController {
    Logger logger = LogManager.getLogger(this.getClass().getName());

    @PostMapping("/user/register")
    public Reply register(@Valid @RequestBody RegisterDto dto) {


        return Reply.fail();
    }

    @PostMapping("/user/login")
    public Reply login(@Valid @RequestBody LoginDto dto) {


        return Reply.fail();
    }

    @PostMapping("/user/change/{username}")
    public Reply change(@Valid @RequestBody ChangeDto dto, @PathVariable String username) {


        return Reply.fail();
    }

    @PostMapping("/index/publish/{username}")
    public Reply publish(@Valid @RequestBody PublishDto dto, @PathVariable String username) {


        return Reply.fail();
    }

    @PostMapping("/index/page/{username}")
    public Reply page(@RequestBody PageDto dto, @PathVariable String username) {


        return Reply.fail();
    }

    @GetMapping("/index/project/{username}/{sn}")
    public Reply project(@PathVariable String username, @PathVariable String sn) {


        return Reply.fail();
    }

}
