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
@Document("message")
@API(value = "消息")
public class Message {

    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "来源", visible = true, search = true)
    String source;

    @API(value = "type", type = "case", choise = {"alert:公告", "inform:通知"}, visible = true)
    String type;

    @API(value = "openid", search = true)
    String openid;

    @API(value = "消息", visible = true, search = true)
    String message;

    @API(value = "状态", type = "case", choise = {"new:未读", "read:已读", "delete:删除"}, visible = true)
    String status = "new";

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time")
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choise = {"0:正常", "1:已删除"})
    int deleted = 0;
}
