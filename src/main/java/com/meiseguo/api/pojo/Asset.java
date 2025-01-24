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
@Document("asset")
@API(value = "数字资产")
public class Asset {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "账户", search = true, visible = true)
    public String account;
    // 哪一种
    @API(value = "币种", search = true, visible = true)
    public String ccy;

    //方向: 多 空
    @API(value = "类型", search = true, type = "case", choise = {"SPOT:现货", "MARGIN:杠杆", "CONTRACT:合约"}, visible = true)
    public String type;

    // 周期：1分钟，15分钟，1小时
    @API(value = "持仓数量", visible = true)
    public double position;

    @API(value = "持仓价值", visible = true)
    public double balance;

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choise = {"0:正常", "1:已删除"})
    int deleted = 0;
}
