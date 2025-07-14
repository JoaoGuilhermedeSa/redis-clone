package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.service.RedisStoreService;

public class PingEventHandler extends AbstractEventHandler {

    public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length == 1) {
            sendSimpleString(out, "PONG");
        } else if (tokens.length == 2) {
            sendBulkString(out, tokens[1]);
        } else {
            sendError(out, "ERR wrong number of arguments for 'ping' command");
        }
        out.flush();
    }
}
