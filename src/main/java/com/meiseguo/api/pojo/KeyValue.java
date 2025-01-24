package com.meiseguo.api.pojo;

import lombok.Data;

@Data
public class KeyValue {
    public static final String delimiter = ":";
    public KeyValue(){}
    public KeyValue(String keyvalue) {
        if(keyvalue.contains(delimiter)) {
            String[] kv = keyvalue.split(delimiter);
            this.key = kv[0];
            this.value = kv[1];
        } else {
            this.key = keyvalue;
            this.value = keyvalue;
        }
    }
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
    String key;
    String value;
}
