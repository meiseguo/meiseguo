package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("log")
@API("LOG")
public class Log {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "来源：某服务，某类", readonly = true, visible = true, search = true)
    String source;

    @API(value = "级别", visible = true, search = true)
    String level;

    @API(value = "内容", readonly = true, visible = true, search = true)
    String message;

    @API(value = "创建时间", visible = true, readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    public Log(){}

    public Log(String source) {
        this.setSource(source);
    }

    public Log source(String source) {
        this.setSource(source);
        return this;
    }

    public Log info(String msg) {
        this.setLevel("INFO");
        this.setMessage(msg);
        return this;
    }
    public Log warn(String msg) {
        this.setLevel("WARN");
        this.setMessage(msg);
        return this;
    }
    public Log error(String msg) {
        this.setLevel("ERROR");
        this.setMessage(msg);
        return this;
    }

    @Override
    public String toString() {
        return  level +'\t' + source  +'\t' + message;
    }
}
