package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import com.meiseguo.api.strategy.INPUT;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Document("action")
@API(value = "下单记录")
public class Action {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "订单号", search = true, visible = true)
    public String order;

    @API(value = "账户", search = true, visible = true)
    public String account;

    @API(value = "ccy", search = true, visible = true)
    public String ccy;

    //方向: 多 空
    @API(value = "类型", type = "case", choise = {"buy:做多", "sell:做空"}, visible = true)
    public String type;

    @API(value = "方向", type = "case", choise = {"buy:买入", "sell:卖出"}, visible = true)
    public String side;

    // 均价
    @API(value = "价格", visible = true)
    public double price;

    @API(value = "数量", visible = true)
    public double amount;

    @API(value = "已平仓数量", visible = true)
    public double closed;

    @API(value = "手续费", visible = true)
    public double fee;

    public long millis;

    @API(value = "状态", type = "case", choise = {"init:创建", "pending:委托", "deal:成交", "cancel:撤销"}, search = true, visible = true)
    public String status;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choise = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Action(Account account, Asset asset, String type) {
        this.account = account.account;
        this.ccy = asset.ccy;
        this.type = type;
    }

    public void sell(double price, double amount) {
        this.side = "sell";
        this.price = price;
        this.amount = amount;
        this.status = "init";
    }

    public void buy(double price, double amount) {
        this.side = "buy";
        this.price = price;
        this.amount = amount;
        this.status = "init";
    }

    public double winRatio(INPUT input) {
        if("buy".equals(type)) {
            return (input.price - this.price) / this.price;
        }

        if("sell".equals(type)) {
            return (this.price - input.price) / this.price;
        }
        return 0;
    }

    public Closed close(Action closed) {
        Closed close = new Closed();
        close.setOpen(this.sn);
        close.setClose(closed.sn);
        close.setCcy(this.ccy);
        close.setPrice(this.price);
        close.setClosed(closed.amount);
        close.setDeal(closed.price);
        close.setStatus("init");
        return close;
    }
}
