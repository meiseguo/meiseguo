package com.meiseguo.api.vo;

import com.meiseguo.api.API;
import com.meiseguo.api.pojo.Series;
import lombok.Data;

import java.util.List;

/**
 * token 接口访问需要验证这个。
 */
@Data
public class StatisticVo {

    @API(value = "marker")
    private RecordVo markerVo;
    @API(value = "record")
    private RecordVo recordVo;

    @API(value = "当天做多")
    private double[] buyToday;
    @API(value = "当天做空")
    private double[] sellToday;
    @API(value = "做多比例")
    private double[] todayBuy;
    @API(value = "做空比例")
    private double[] todaySell;
    @API(value = "下标")
    private List<String> winToday;
    @API(value = "当天")
    private String sumToday;
    @API(value = "详情")
    private String todayBS;
    @API(value = "比例")
    private String ratioBS;

    @API(value = "做多")
    private double[] buy;
    @API(value = "做空")
    private double[] sell;
    @API(value = "做多回撤")
    private double[] buyD;
    @API(value = "做空回撤")
    private double[] sellD;

    @API(value = "在途订单")
    private List<String> xAxis;

    @API(value = "已平仓")
    private List<String> xClosed;

    @API(value = "投入产出")
    private List<String> pie;

    @API(value = "投资比例")
    private double[] invest;

    @API(value = "累计盈利")
    private double[] ptdClosed;

    @API(value = "价格波动")
    private List<Series> series;

    @API(value = "价格名称")
    private List<String> legend;

    @API(value = "count")
    private int[] count;
    @API(value = "最高价")
    private double high;
    @API(value = "最低价")
    private double low;
    @API(value = "当前价")
    private double current;

    @API(value = "连续跌")
    private int desc;
    @API(value = "连续涨")
    private int asc;
    @API(value = "剩余时间")
    private String timeout;
}
