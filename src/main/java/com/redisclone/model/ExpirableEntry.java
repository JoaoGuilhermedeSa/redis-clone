package com.redisclone.model;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ExpirableEntry implements Delayed {
    private final String key;
    private final long expiryTime; // Absolute expiration time in millis

    public ExpirableEntry(String key, long expiryTime) {
        this.key = key;
        this.expiryTime = expiryTime;
    }

    public String getKey() {
        return key;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remaining = expiryTime - System.currentTimeMillis();
        return unit.convert(remaining, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpirableEntry that = (ExpirableEntry) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}