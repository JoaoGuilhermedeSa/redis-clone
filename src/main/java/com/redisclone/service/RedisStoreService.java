package com.redisclone.service;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ExpirableEntry;
import com.redisclone.model.RedisObject;

public class RedisStoreService {
	
	private static final Logger logger = LoggerFactory.getLogger(RedisStoreService.class);
	
    private final ConcurrentHashMap<String, RedisObject> dataStore;
    private final ExpirationManager expManager;
    private final Thread cleanerThread;

    public RedisStoreService() {
        this.dataStore = new ConcurrentHashMap<>();
        this.expManager = new ExpirationManager();
        this.cleanerThread = this.createCleanerThread();
    }

    public void setWithExpiry(String key, RedisObject value, long seconds) {
        dataStore.put(key, value);
        if (seconds > 0) {
            long absolute = System.currentTimeMillis() + seconds * 1000;
            value.setExpireAt(absolute);
            expManager.updateExpiry(key, absolute);
        } else {
            expManager.removeExpiry(key);
        }
    }
    
    public RedisObject get(String key) {
    	RedisObject obj = dataStore.get(key);
		if (obj != null && obj.isExpired()) {
			remove(key);
			return null;
		}
    	return dataStore.get(key);
    }
    
    public void removeExpiry(String key) {
    	expManager.removeExpiry(key);
    }
    
    public RedisObject remove(String key) {
    	expManager.removeExpiry(key);
    	return dataStore.remove(key);
    }
    
    public void shutdownCleanerThread() {
        cleanerThread.interrupt();
    }

	public void updateExpiry(String key, long absoluteExpiry) {
		this.expManager.updateExpiry(key, absoluteExpiry);		
	}

	public void put(String string, RedisObject obj) {
		dataStore.put(string, obj);		
	}

    private Thread createCleanerThread() {
        Thread cleanerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ExpirableEntry entry = expManager.getExpiryQueue().take();
                    String key = entry.getKey();
                    ExpirableEntry current = expManager.getExpiryMap().get(key);
                    if (current == entry) {
                    	expManager.getExpiryMap().remove(key);
                        dataStore.remove(key);
                        logger.debug("Expired and removed key: {}", key);
                    }
                } catch (InterruptedException e) {
                    logger.info("Cleaner thread interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanerThread.setDaemon(true);
        cleanerThread.start();
        return cleanerThread;
    }    
}