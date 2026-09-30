package com.meiseguo.api.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class TimeLock {
    private long duration;
    private String key;
    private long until;
    private AtomicBoolean locked;

    public TimeLock(long timeMs, String key, long seconds) {
        this.key = key;
        this.duration = seconds;
        this.until = timeMs + duration * 1000;
        this.locked = new AtomicBoolean(false);
    }

    public boolean locked(long timeMs) {
        if (timeMs > until && locked.compareAndSet(false, true)) {
            until = timeMs + duration * 1000;
            locked.set(false);
            return true;
        }
        return false;
    }

}
