package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.RedisObject;

public class ExpireEventHandler implements EventHandler {

    public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens, PrintWriter out) {
        if (tokens.length == 3) {
            String key = tokens[1];
            RedisObject obj = dataStore.get(key);
            if (obj != null) {
                try {
                    long seconds = Long.parseLong(tokens[2]);
                    if (seconds < 0) {
                        out.println("(error) ERR invalid expire time in expire");
                        return;
                    }
                    if (seconds == 0) {
                        dataStore.remove(key);
                        expManager.removeExpiry(key);
                        out.println("(integer) 1");
                    } else {
                        long absoluteExpiry = System.currentTimeMillis() + seconds * 1000;
                        obj.setExpireAt(absoluteExpiry);
                        expManager.updateExpiry(key, absoluteExpiry);
                        out.println("(integer) 1");
                    }
                } catch (NumberFormatException e) {
                    out.println("(error) ERR value is not an integer or out of range");
                }
            } else {
                out.println("(integer) 0");
            }
        } else {
            out.println("(error) ERR wrong number of arguments for 'expire' command");
        }
    }

}