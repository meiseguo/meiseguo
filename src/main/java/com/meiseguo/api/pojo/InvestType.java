package com.meiseguo.api.pojo;

public enum InvestType {
    all("总共"), day("每日"), time("实时");
    InvestType(String desc){
        this.desc = desc;
    }
    private final String desc;
    public String desc() {
        return desc;
    }
}
