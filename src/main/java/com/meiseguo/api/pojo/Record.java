package com.meiseguo.api.pojo;

import com.meiseguo.api.strategy.INPUT;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Record implements Function<INPUT, Optional<Statistic>> {
    long duration;

    List<INPUT[]> lowHigh;

    public Record() {
        duration = TimeUnit.MINUTES.toMillis(15);
        lowHigh = new ArrayList<>();
    }
    public Record(long millis) {
        duration = millis;
        lowHigh = new ArrayList<>();
    }

    @Override
    public Optional<Statistic> apply(INPUT input) {
        if (lowHigh.isEmpty()) {
            lowHigh.add(new INPUT[] {input, input});
        }
        //1. 时间间隔相差15min了吗？那就另外启动一个
        INPUT[] record = lowHigh.get(lowHigh.size() - 1);
        if (input.millis - record[0].millis > duration) {
            lowHigh.clear();
            INPUT[] newRecord = new INPUT[] {record[1], input};
            lowHigh.add(record);
            lowHigh.add(newRecord);
            return Optional.of(new Statistic(record[0], record[1]));
        } else {
            int lowIndex = record[0].price > record[1].price ? 1 : 0;
            int highIndex = record[0].price > record[1].price ? 0 : 1;

            if (input.price < record[lowIndex].price) {
                record[lowIndex] = input;
            }

            if (input.price > record[highIndex].price) {
                record[highIndex] = input;
            }
            Arrays.sort(record, Comparator.comparingDouble(o -> o.millis));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (INPUT[] records : lowHigh) {
            sb.append("\n").append(records[0]).append("\n").append(records[1]).append("\n");
        }
        return "Record{" + "duration=" + duration + ", lowHigh=" + sb + '}';
    }
}
