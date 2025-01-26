package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Document("order")
@API(value = "订单")
public class Order {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "账户", search = true, visible = true)
    public String account;
    // 哪一种
    @API(value = "币种", search = true, visible = true)
    public String ccy;

    //方向: 多 空
    @API(value = "类型", search = true, type = "case", choice = {"SPOT:现货", "MARGIN:杠杆", "OPTION:合约", "FUTURES:期货", "SWAP:永续", "ANY:任意"}, visible = true)
    String instType;
    String instId;
    String tgtCcy;
    String ordId;
    String clOrdId;
    String tag;
    @API("Price")
    String px;
    String pxUsd;
    String pxVol;
    String pxType;
    String sz;
    String notionalUsd;
    @API(value = "Order type", type = "case", choice = {
            "market: market order",
            "limit: limit order",
            "post_only: Post-only order",
            "fok: Fill-or-kill order",
            "ioc: Immediate-or-cancel order",
            "optimal_limit_ioc: Market order with immediate-or-cancel order (applicable only to Expiry Futures and Perpetual Futures)",
            "mmp: Market Maker Protection (only applicable to Option in Portfolio Margin mode)",
            "mmp_and_post_only: Market Maker Protection and Post-only order(only applicable to Option in Portfolio Margin mode).",
            "op_fok: Simple options (fok)"})
    String ordType;
    @API(value = "Order side", type = "case", choice = {"buy:买入", "sell:卖出"})
    String side;
    String posSide;
    @API(value = "Trade mode", type = "case", choice = {"cross:cross","isolated:isolated","cash: cash"})
    String tdMode;
    String fillPx;
    String tradeId;
    String fillSz;
    String fillPnl;
    String fillTime;
    String fillFee;
    String fillFeeCcy;
    String fillPxVol;
    String fillPxUsd;
    String fillMarkVol;
    String fillFwdPx;
    String fillMarkPx;
    String execType;
    String accFillSz;
    String fillNotionalUsd;
    String avgPx;
    @API(value = "状态", type = "case", choice = {
            "canceled:取消",
            "live:委托",
            "partially_filled:部分成交",
            "filled:成交",
            "mmp_canceled:被动取消"})
    String state;
    String lever;
    String attachAlgoClOrdId;
    String tpTriggerPx;
    String tpTriggerPxType;
    String tpOrdPx;
    String slTriggerPx;
    String slTriggerPxType;
    String slOrdPx;
    String stpId;
    String stpMode;
    String feeCcy;
    String fee;
    String rebateCcy;
    String rebate;
    String pnl;
    String source;
    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
