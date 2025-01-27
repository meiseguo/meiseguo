package com.meiseguo.api.strategy;

import lombok.Data;

@Data
public class INPUT {
    public INPUT(){}
    public INPUT(double price) {
        this.millis = System.currentTimeMillis();
        this.price = price;
    }
    public long millis;
    public double price;
}
