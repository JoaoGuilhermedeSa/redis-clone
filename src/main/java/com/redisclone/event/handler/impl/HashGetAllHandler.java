package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.Map;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class HashGetAllHandler extends AbstractEventHandler {

    public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length != 2) {
            sendError(out, "ERR wrong number of arguments for 'hgetall' command");
            return;
        }
        String key = tokens[1];
        RedisObject obj = redisStoreService.get(key);
        if (obj == null || obj.isExpired()) {
            sendEmptyArray(out);
            return;
        }
        if (obj.getType() != ObjectType.HASH) {
            sendError(out, "WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> hash = (Map<String, String>) obj.getValue();
        if (hash.isEmpty()) {
            sendEmptyArray(out);
            return;
        }
        
        out.print("*" + (hash.size() * 2) + "\r\n");
        for (Map.Entry<String, String> entry : hash.entrySet()) {
            sendBulkString(out, entry.getKey());
            sendBulkString(out, entry.getValue());
        }
        out.flush(); // Ensure the response is sent immediately
    }


}