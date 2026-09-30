package com.meiseguo.api.pojo;


import com.meiseguo.api.API;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("case")
@API("分支判断")
public class Case {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "理由", search = true, visible = true)
    String reason;

    public String getReason() {
        return operator + ":" + desc + (result?"✅":"×");
    }

    @API(value = "traceid", readonly = true, search = true)
    String traceid;

    @API(value = "操作员", search = true)
    String operator;

    @API(value = "判断条件", readonly = true, search = true)
    String desc;

    @API(value = "结论", type = "case", choice = {"true:✅","false:×"}, readonly = true)
    boolean result;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    public Case is(boolean r) {
        this.result = r;
        return this;
    }

    public static Case when(Mode mode, String desc) {
        Case c = new Case();
        c.desc = mode.name() + ":" + desc;
        return c;
    }
}
