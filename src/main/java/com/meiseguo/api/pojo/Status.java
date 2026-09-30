package com.meiseguo.api.pojo;

import com.meiseguo.api.API;
import com.meiseguo.api.utils.PagesUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 钱包登录前保存nonce，下一步验证。
 */
@Data
@Accessors(chain = true)
@Document("status")
@API(value = "投资状态")
public class Status {
    @Id
    @API(value = "sn", readonly = true)
    ObjectId sn = new ObjectId();

    @API(value = "操作员", search = true, visible = true)
    public String operator;

    @API(value = "浮盈数量", visible = true)
    public long winCount;

    @API(value = "浮亏数量", visible = true)
    public long lossCount;

    @API(value = "浮盈", visible = true)
    public double winValue;

    public String getWinValue() {
        return PagesUtil.numbers(winValue);
    }

    @API(value = "浮亏", visible = true)
    public double lossValue;

    public String getLossValue() {
        return PagesUtil.numbers(lossValue);
    }

    @API(value = "连赢", visible = true)
    public long winWinCount;
    @API(value = "连输", visible = true)
    public long lossLossCount;
    @API(value = "最多赢", visible = true)
    public long winMaxCount;
    @API(value = "最多输", visible = true)
    public long lossMaxCount;

    @API(value = "限输", visible = true)
    public long limitLossCount;
    @API(value = "限赢", visible = true)
    public long limitWinCount;

    //状态: 连赢 连亏
    @API(value = "状态", type = "case", choice = {"init:初始", "win:止盈", "loss:止损"}, visible = true)
    public String status = "init";

    @API(value = "创建时间", readonly = true, type = "time")
    LocalDateTime createtime = LocalDateTime.now();

    @API(value = "更新时间", type = "time", visible = true)
    LocalDateTime updatetime = LocalDateTime.now();

    @API(value = "软删除", type = "case", choice = {"0:正常", "1:已删除"})
    int deleted = 0;
}
