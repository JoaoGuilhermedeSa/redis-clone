package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.service.RedisStoreService;

public class DelEventHandler implements EventHandler {

    @Override
    public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length < 2) {
            out.println("(error) wrong number of arguments for 'del' command");
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