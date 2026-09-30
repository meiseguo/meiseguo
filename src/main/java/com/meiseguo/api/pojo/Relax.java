package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Document("relax")
@API(value = "冷静期", remote = true, source = "relax")
public class Relax {
    public Relax() {
        this.operator = "null";
    }
    public Relax(String operator, String reason) {
        this.operator = operator;
        this.reason = reason;
    }

    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "操作员", visible = true, search = true)
    public String operator;

    @API(value = "理由", visible = true, search = true)
    public String reason;

    @API(value = "秒", visible = true)
    public int delay = 10;

    @API(value = "下一次", visible = true)
    public LocalDateTime until = LocalDateTime.now().plusSeconds(delay);

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    public boolean relax() {
        return LocalDateTime.now().isBefore(until);
    }

    public void calm(long seconds) {
        delay = (int) seconds;
        until = LocalDateTime.now().plusSeconds(seconds);
    }
}
