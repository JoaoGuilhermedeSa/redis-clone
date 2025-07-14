package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.service.RedisStoreService;

public class EchoEventHandler extends AbstractEventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length == 2) {
            out.println("\"" + tokens[1] + "\"");
        } else {
            sendError(out, "ERR wrong number of arguments for 'get' command");
        }
	}

}
