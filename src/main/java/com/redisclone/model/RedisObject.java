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
        if (expireAt == -1) {
            return false;
        }
        return System.currentTimeMillis() > expireAt;
    }
}