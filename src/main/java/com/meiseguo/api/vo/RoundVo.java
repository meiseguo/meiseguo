package com.meiseguo.api.vo;

import com.meiseguo.api.pojo.Follow;
import com.meiseguo.api.pojo.Round;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class RoundVo {
    public ObjectId sn = new ObjectId();
    public String operator;
    public String round;
    public String ccy;
    public String type;
    public double amount;
    public double openPrice;
    public double closePrice;
    public double value;
    public String status;
    LocalDateTime createtime = LocalDateTime.now();
    LocalDateTime updatetime = LocalDateTime.now();
    List<Follow> followList;
    public RoundVo(){}

    public RoundVo(Round round, List<Follow> followList) {
        this.sn = round.getSn();
        this.operator = round.getOperator();
        this.round = round.getRound();
        this.ccy = round.getCcy();
        this.type = round.getType();
        this.amount = 0;
        this.openPrice = 0;
        this.closePrice = 0;
        this.value = 0;
        this.status = round.getStatus();
        this.createtime = round.getCreatetime();
        this.updatetime = round.getUpdatetime();
        this.followList = followList;
    }
}
