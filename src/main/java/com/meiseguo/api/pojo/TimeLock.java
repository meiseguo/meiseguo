package com.meiseguo.api.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class TimeLock {
    private long duration;
    private String key;
    private AtomicLong until;

    public TimeLock(long timeMs, String key, long seconds) {
        this.key = key;
        this.duration = seconds;
        this.until = new AtomicLong(timeMs + duration * 1000);
    }

    public boolean locked(long timeMs) {
        long current = until.get();
        if (timeMs > current) {
            return until.compareAndSet(current, timeMs + duration * 1000);
        }
        return false;
    }

}
