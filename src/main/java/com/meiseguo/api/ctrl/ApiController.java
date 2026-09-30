package com.meiseguo.api.ctrl;

import com.meiseguo.api.pojo.Operator;
import com.meiseguo.api.pojo.Order;
import com.meiseguo.api.pojo.Reply;
import com.meiseguo.api.service.RoundService;
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
    private StrategyService strategyService;

    @Autowired
    private RoundService roundService;

    @PostMapping("/statistic/{ccy}")
    public Reply statistic(@PathVariable String ccy) {
        try {
            return Reply.success(strategyService.statistic(ccy));
        } catch (Exception e) {
            logger.error("Error", e);
            return Reply.fail(e.getMessage());
        }
    }

    @PostMapping("/actions/{account}")
    public Reply actions(@PathVariable String account) {
        try {
            long timeMs = System.currentTimeMillis();
            return Reply.success(strategyService.actions(account, timeMs));
        } catch (Exception e) {
            logger.error("Error", e);
            return Reply.fail(e.getMessage());
        }
    }

    // TODO secure
    @PostMapping("/accounts")
    public Reply accounts() {
        return Reply.success(strategyService.accounts());
    }

    @PostMapping("/assets")
    public Reply assets() {
        return Reply.success(strategyService.assets());
    }

    @PostMapping("/ticker/{ccy}/{price}/{ts}")
    public Reply ticker(@PathVariable String ccy, @PathVariable double price, @PathVariable long ts) {
        try {
            strategyService.ticker(ccy, price, ts);
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return Reply.success();
    }

    @PostMapping("/danger/{account}/{instId}/{instType}/{mgnMode}/{liqPx}/{avgPx}")
    public Reply liqPx(@PathVariable String account,
                       @PathVariable String instId,
                       @PathVariable String instType,
                       @PathVariable String mgnMode,
                       @PathVariable double liqPx,
                       @PathVariable double avgPx) {
        return Reply.success(strategyService.liqPx(account, instId, instType, mgnMode, liqPx, avgPx));
    }

    @PostMapping("/update/{ordId}/{sn}/{status}")
    public Reply update(@PathVariable String ordId, @PathVariable String sn, @PathVariable String status) {
        strategyService.update(ordId, sn, status);
        return Reply.success();
    }

    @PostMapping("/report/{account}")
    public Reply report(@PathVariable String account, @RequestBody Order order) {
        strategyService.report(account, order);
        return Reply.success();
    }


    @PostMapping("/round/{operator}/{type}/{unit}")
    public Reply round(@PathVariable String operator, @PathVariable String type, @PathVariable Double unit) {
        Operator op = strategyService.operators().stream().filter(o -> o.getOperator().equals(operator)).findFirst().orElseThrow(() -> new RuntimeException("不存在的" + operator));
        return Reply.success(roundService.createRound(op, type, unit));
    }

    @PostMapping("/round/list")
    public Reply roundList() {
        return Reply.success(roundService.getRounds());
    }

    @PostMapping("/round/follow/{sn}")
    public Reply follow(@PathVariable String sn) {
        return Reply.success(roundService.follow(sn));
    }

    @PostMapping("/round/reduce/{sn}")
    public Reply reduce(@PathVariable String sn) {
        roundService.reduce(sn);
        return Reply.success();
    }

    @PostMapping("/round/now/{sn}")
    public Reply now(@PathVariable String sn) {
        roundService.now(sn);
        return Reply.success();
    }

    @PostMapping("/round/closing/{sn}")
    public Reply closing(@PathVariable String sn) {
        return Reply.success(roundService.closing(sn));
    }

}
