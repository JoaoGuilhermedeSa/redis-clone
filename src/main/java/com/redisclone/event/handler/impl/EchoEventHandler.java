package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.RedisObject;

public class EchoEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens, PrintWriter out) {
        if (tokens.length == 2) {
            out.println("\"" + tokens[1] + "\"");
        } else {
            out.println("(error) ERR wrong number of arguments for 'echo' command");
        }
	}

}
