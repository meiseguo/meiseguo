package com.meiseguo.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ApiHead {
    //参数是否展示
    boolean visible;
    boolean readonly;
    //参数名称
    String name;
    //参数展示值
    String value;
    //类型 str url date time case
    String type;
    List<KeyValue> choise;
    String source;
}
