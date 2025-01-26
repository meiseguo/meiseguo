package com.meiseguo.api.pojo;

public enum ActionStatus {
    init("创建"), live("委托"), filled("已成交"), canceled("已取消");
    ActionStatus(String desc){}
}
