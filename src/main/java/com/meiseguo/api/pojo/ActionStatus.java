package com.meiseguo.api.pojo;

public enum ActionStatus {
    init("创建"), live("委托"), filled("已成交"), cancel("请撤销"), canceled("撤销"), closing("请平仓"), closed("平仓"), error("异常");
    ActionStatus(String desc){}
}
