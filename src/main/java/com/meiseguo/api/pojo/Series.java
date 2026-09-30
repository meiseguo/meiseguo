package com.meiseguo.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Series {
    String type;
    String name;
    double[] data;
    String symbol;
}
