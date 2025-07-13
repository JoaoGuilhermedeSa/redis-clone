package com.redisclone.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import com.redisclone.model.ExpirableEntry;

public class ExpirationManager {
    private final DelayQueue<ExpirableEntry> expiryQueue = new DelayQueue<>();
    private final ConcurrentHashMap<String, ExpirableEntry> expiryMap = new ConcurrentHashMap<>();

    public void updateExpiry(String key, long absoluteExpiryTime) {
        ExpirableEntry oldEntry = expiryMap.remove(key);
        if (oldEntry != null) {
            expiryQueue.remove(oldEntry);
        }
        if (absoluteExpiryTime > 0) {
            ExpirableEntry entry = new ExpirableEntry(key, absoluteExpiryTime);
            expiryMap.put(key, entry);
            expiryQueue.offer(entry);
        }
    }

    public void removeExpiry(String key) {
        ExpirableEntry entry = expiryMap.remove(key);
        if (entry != null) {
            expiryQueue.remove(entry);
        }
    }

    public DelayQueue<ExpirableEntry> getExpiryQueue() {
        return expiryQueue;
    }

    public ConcurrentHashMap<String, ExpirableEntry> getExpiryMap() {
        return expiryMap;
    }
}