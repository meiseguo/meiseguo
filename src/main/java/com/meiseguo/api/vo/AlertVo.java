package com.meiseguo.api.vo;

import com.meiseguo.api.API;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("alert")
@API(value = "通知消息")
public class AlertVo {

    @API(value = "type", type = "case", choise = {"alert:公告", "url:链接", "commodity:商品"}, visible = true)
    String type;
    @API(value = "商品sn")
    String refid;
    @API(value = "链接地址")
    String url;//pages/detail?sn=aabiaddas

    @API(value = "openid", search = true)
    String openid;
    @API(value = "消息", visible = true, search = true)
    String message;
    @API(value = "标题", visible = true, search = true)
    String title;

    @API(value = "时间", visible = true, search = true)
    String time;

    @API(value = "状态", type = "case", choise = {"new:未读", "read:已读"}, visible = true)
    String status = "new";
}
