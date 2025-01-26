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
@Document("alarm")
@API(value = "预警")
public class Alarm {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "账户", search = true, visible = true)
    public String account;
    // 哪一种
    @API(value = "币种", search = true, visible = true)
    public String ccy;

    //方向: 多 空
    @API(value = "类型", search = true, type = "case", choice = {"price:价格预警", "position:仓位预警", "balance:余额预警"}, visible = true)
    public String type;

    @API(value = "备注", search = true, visible = true)
    public String remark;

    // 周期：1分钟，15分钟，1小时
    @API(value = "最高价格", visible = true)
    public double high;

    @API(value = "最低价格", visible = true)
    public double low;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
