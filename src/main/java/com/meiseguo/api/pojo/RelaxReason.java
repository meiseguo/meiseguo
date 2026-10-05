package com.meiseguo.api.pojo;

public enum RelaxReason {
    empty("未开仓"), open("补仓"), close("减仓"), win("止盈"), loss("止损");
    private final String desc;
    RelaxReason(String desc){
        this.desc = desc;
    }
    public String desc() {
        return desc;
    }
}
