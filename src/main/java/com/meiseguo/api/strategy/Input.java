package com.meiseguo.api.strategy;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("input")
@API(value = "数据")
public class Input {
    public Input(){}
    public Input(String ccy, double price, long ts) {
        this.millis = ts;
        this.price = price;
        this.ccy = ccy;
    }
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "ccy", search = true, visible = true)
    public String ccy;

    @API(value = "millis", visible = true)
    public long millis;

    @API(value = "price", visible = true)
    public double price;
}
