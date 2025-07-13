package com.redisclone.model;

public class RedisObject {

    private final ObjectType type;
    private final Object value;
    private long expireAt = -1; // -1 means no expiry

    public RedisObject(ObjectType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ObjectType getType() {
        return type;
    }
    
    public long getRemainingTTL() {
        if (expireAt <= 0) return -1;
        long remaining = expireAt - System.currentTimeMillis();
        if (remaining <= 0) return -2;
        return remaining / 1000; // in seconds, as per Redis TTL
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public boolean isExpired() {
        return expireAt > 0 && System.currentTimeMillis() >= expireAt;
    }
}