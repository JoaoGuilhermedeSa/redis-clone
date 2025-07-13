package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.service.RedisStoreService;

public class EchoEventHandler implements EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
        if (tokens.length == 2) {
            out.println("\"" + tokens[1] + "\"");
        } else {
            out.println("(error) ERR wrong number of arguments for 'echo' command");
        }
	}

}
