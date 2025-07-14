package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.service.RedisStoreService;

public class DelEventHandler extends AbstractEventHandler {

    @Override
    public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length < 2) {
            sendError(out, "ERR wrong number of arguments for 'get' command");
            return;
        }
        int deleted = 0;
        for (int i = 1; i < tokens.length; i++) {
            String key = tokens[i];
            if (redisStoreService.remove(key) != null) {
                deleted++;
            }
        }
        out.println(":" + deleted);
    }
}