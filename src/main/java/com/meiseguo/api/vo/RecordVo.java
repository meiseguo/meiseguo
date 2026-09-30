package com.meiseguo.api.vo;

import com.meiseguo.api.API;
import lombok.Data;

import java.util.List;

/**
 * token 接口访问需要验证这个。
 */
@Data
public class RecordVo {

    @API(value = "index")
    private List<String> index;
    @API(value = "RSI")
    private int rsi;

    @API(value = "涨幅")
    private double[] up;

    @API(value = "跌幅")
    private double[] down;

    @API(value = "平均涨幅")
    private double[] upAverage;

    @API(value = "平均跌幅")
    private double[] downAverage;

    @API(value = "最新")
    private double[] latest;

    @API(value = "反弹")
    private double[] bounce;

    @API(value = "当前涨跌")
    private double upDown;
    @API(value = "反弹")
    private double goBack;
}
