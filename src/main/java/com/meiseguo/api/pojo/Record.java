package com.meiseguo.api.pojo;

import com.meiseguo.api.strategy.Input;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Record implements Function<Input, Optional<Statistic>> {
    public static final int LIMIT = 50;
    public long duration;
    public final LinkedList<Input[]> lowHigh;
    public static final int START = 0;
    public static final int END = 1;
    public static final int SIZE = 10;
    public double small;
    public Input current;
    public Predicate<Input[]> up = x -> x[START].price < x[END].price;
    public ToDoubleFunction<Input[]> winRate = x -> Math.abs(x[START].price - x[END].price) / x[START].price;
    public AtomicInteger bull = new AtomicInteger(0);
    public AtomicInteger bear = new AtomicInteger(0);
    public AtomicInteger index = new AtomicInteger(0);
    public Record() {
        this(33, new LinkedList<>(), 0.003);
    }

    public Record(long minutes, LinkedList<Input[]> list, double small) {
        this.duration = TimeUnit.MINUTES.toMillis(minutes);
        this.lowHigh = list;
        this.small = small;
    }

    public Record(List<Input[]> history) {
        this(33, new LinkedList<>(history), 0.003);
    }

    @Override
    public Optional<Statistic> apply(Input input) {
        if(input == null) {
            return Optional.empty();
        }
        this.index.incrementAndGet();
        this.current = input;
        synchronized (lowHigh) {
            if (lowHigh.isEmpty()) {
                lowHigh.offer(new Input[]{input, input});
            }
            //1. 时间间隔相差duration了吗？那就另外启动一个
            final Input[] record = lowHigh.getLast();
            if (input.millis - record[START].millis > duration) {
                if (lowHigh.size() > 1) {
                    // 合并最后2个（如果是同一个走势就合并）
                    Input[] last = lowHigh.pollLast();
                    Input[] next = lowHigh.pollLast();
                    Input[] merge = merge(last, next);
                    if (merge == last && next != null) {
                        lowHigh.offer(next);
                    }
                    if (merge != null) {
                        lowHigh.offer(merge);
                    }
                }
                if (lowHigh.size() > LIMIT) {
                    lowHigh.poll();
                }
                Input[] latest = lowHigh.getLast();
                Input[] newRecord = new Input[]{latest[END], input};
                lowHigh.offer(newRecord);
                return Optional.of(new Statistic(latest[START], latest[END]));
            } else {
                int low = START;
                int high = END;
                if (up.negate().test(record)) {
                    low = END;
                    high = START;
                }
                if (input.price < record[low].price) {
                    record[low] = input;
                }
                if (input.price > record[high].price) {
                    record[high] = input;
                }
                Arrays.sort(record, Comparator.comparingLong(Input::getMillis));
            }
        }
        return Optional.empty();
    }

    private Input[] merge(Input[] last, Input[] next) {
        if(next == null) {
            return last;
        }
        if (up.test(last) && up.test(next) && last[START].price >= next[END].price) {
            return new Input[]{next[START], last[END]};
        }

        if (up.negate().test(last) && up.negate().test(next) && last[START].price <= next[END].price) {
            return new Input[]{next[START], last[END]};
        }
        return last;
    }

    public Input current() {
        return current;
    }

    public double average(double dir) {
        Predicate<Input[]> filter = dir > 0 ? up : up.negate();
        if(lowHigh.size() < 2) return small;
        LinkedList<Input[]> select = new LinkedList<>(lowHigh);
        select.pollLast();
        List<Input[]> collect = select.stream().filter(filter).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return small;
        }
        double[] array = collect.stream().mapToDouble(winRate).toArray();
        return DoubleStream.of(array).skip(Math.max(0, array.length - 3)).min().orElse(small);
    }

    public double decline() {
        Input[] latest = lowHigh.getLast();
        Input last = latest[END];
        return (current.price - last.price) / last.price;
    }

    public long timeLeft() {
        Input[] latest = lowHigh.getLast();
        Input first = latest[START];
        return duration - (current.millis - first.millis);
    }

    public double latest() {
        Input[] latest = lowHigh.getLast();
        return (current.price - latest[START].price) / latest[START].price;
    }

    public boolean low() {
        if(lowHigh.isEmpty()) return false;
        Input[] latest = lowHigh.getLast();
        Input first = latest[START];
        boolean startLow = current.price < first.price && (first.price - current.price) / first.price >= average(-1);

        Input last = latest[END];
        double drawdown = (last.price - current.price) / last.price;
        boolean bounce = current.price < last.price && drawdown >= average(-1);
        return (startLow && Math.abs(drawdown) < small) || bounce;
    }

    public int nextTimes(StrategyType type) {
        if(lowHigh.isEmpty()) return 0;

        if(type == StrategyType.sell) {
            LinkedList<Input[]> select = new LinkedList<>(lowHigh.subList(Math.max(0, lowHigh.size() - SIZE), lowHigh.size()));
            if(select.isEmpty()) {
                return 0;
            }
            bull.set(0);
            while(!select.isEmpty() && up.test(select.pollLast())) {
                bull.incrementAndGet();
            }
            return bull.get();
        } else {
            LinkedList<Input[]> select = new LinkedList<>(lowHigh.subList(Math.max(0, lowHigh.size() - SIZE), lowHigh.size()));
            if(select.isEmpty()) {
                return 0;
            }
            bear.set(0);
            while (!select.isEmpty() && up.negate().test(select.pollLast())) {
                bear.incrementAndGet();
            }
            return bear.get();
        }
    }

    public double rsi() {
        LinkedList<Input[]> select = new LinkedList<>(lowHigh.subList(Math.max(0, lowHigh.size() - SIZE), lowHigh.size()));
        if(select.isEmpty()) {
            return 0;
        }
        double rsA = 0.0;
        double rsB = 0.0;
        long counter = 0;

        while (!select.isEmpty()) {
            Input[] inputs = select.pollLast();
            long timeDiff = inputs[END].millis - inputs[START].millis;
            double priceDiff = inputs[END].price - inputs[START].price;
            counter += timeDiff;
            if(priceDiff > 0) {
                rsA += priceDiff * timeDiff;
            } else {
                rsB += Math.abs(priceDiff) * timeDiff;
            }
        }
        double rsAAvg = rsA / counter;
        double rsBAvg = rsB / counter;
        if(rsBAvg == 0) {
            return 100;
        } else if(rsAAvg == 0) {
            return 0;
        }
        double rs = rsAAvg / rsBAvg;
        return 100 - (100 / (1 + rs));
    }

    public boolean high() {
        if(lowHigh.isEmpty()) return false;
        Input[] latest = lowHigh.getLast();
        Input first = latest[START];
        boolean startHigh = current.price > first.price && (current.price - first.price) / first.price >= average(1);

        Input last = latest[END];
        double drawdown = (current.price - last.price) / last.price;
        boolean bounce = current.price > last.price && drawdown >= average(1);
        return (startHigh && Math.abs(drawdown) < small) || bounce;
    }

    public double max(int dir) {
        Predicate<Input[]> filter = dir > 0 ? up : up.negate();
        if(lowHigh.size() < 2) return small;
        LinkedList<Input[]> select = new LinkedList<>(lowHigh.subList(Math.max(0, lowHigh.size() - SIZE), lowHigh.size() - 1));
        List<Input[]> collect = select.stream().filter(filter).collect(Collectors.toList());
        return collect.stream().mapToDouble(winRate).max().orElse(small);
    }
}
