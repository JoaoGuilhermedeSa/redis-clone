package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class SetEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens,
			PrintWriter out) {
		if (tokens.length == 3) {
			dataStore.put(tokens[1], new RedisObject(ObjectType.STRING, tokens[2]));
			out.println("OK");
		} else {
			out.println("(error) ERR wrong number of arguments for 'set' command");
		}
	}

}
