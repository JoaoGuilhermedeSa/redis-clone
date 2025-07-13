package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class ExpireEventHandler implements EventHandler {

    public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length == 3) {
            String key = tokens[1];
            RedisObject obj = redisStoreService.get(key);
            if (obj != null) {
                try {
                    long seconds = Long.parseLong(tokens[2]);
                    if (seconds < 0) {
                        out.println("(error) ERR invalid expire time in expire");
                        return;
                    }
                    if (seconds == 0) {
                    	redisStoreService.remove(key);
                        out.println("(integer) 1");
                    } else {
                        long absoluteExpiry = System.currentTimeMillis() + seconds * 1000;
                        obj.setExpireAt(absoluteExpiry);
                        redisStoreService.updateExpiry(key, absoluteExpiry);
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