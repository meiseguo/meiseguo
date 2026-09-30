package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * access 客户登录之后才会有access，有了这个才算授权通过。
 */
@Data
@Accessors(chain = true)
@Document("proc")
@API(value = "进程", remote = true, source = "proc")
public class Proc {
    public Proc(){}
    public Proc(String shell){
        this.shell = shell;
    }

    public Proc(String pid, String shell){
        this.pid = pid;
        this.shell = shell;
    }
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "pid", visible = true, search = true)
    String pid;

    @API(value = "shell", visible = true, search = true)
    String shell;

    @API(value = "消息", search = true)
    String message;

    @API(value = "状态", type = "case", choice = {"init:初始化", "running:运行中", "kill:结束", "wait:稍等"})
    String status = "init";

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Proc proc = (Proc) o;
        return Objects.equals(shell, proc.shell);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shell);
    }
}
