package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class GetEventHandler extends AbstractEventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length == 3) {
            String key = tokens[1];
            RedisObject obj = redisStoreService.get(key);
            if (obj != null) {
                try {
                    long seconds = Long.parseLong(tokens[2]);
                    if (seconds < 0) {
                        sendError(out, "ERR invalid expire time in expire");
                        return;
                    }
                    if (seconds == 0) {
                        redisStoreService.remove(key);
                        sendSimpleInteger(out, 1);
                    } else {
                        long absoluteExpiry = System.currentTimeMillis() + seconds * 1000;
                        obj.setExpireAt(absoluteExpiry);
                        redisStoreService.updateExpiry(key, absoluteExpiry);
                        sendSimpleInteger(out, 1);
                    }
                } catch (NumberFormatException e) {
                    sendError(out, "ERR value is not an integer or out of range");
                }
            } else {
                sendSimpleInteger(out, 0);
            }
        } else {
            sendError(out, "ERR wrong number of arguments for 'expire' command");
        }
        out.flush();
    }

}