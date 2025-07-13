package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class SetExEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens, PrintWriter out) {
		if (tokens.length == 4) {
			try {
				long seconds = Long.parseLong(tokens[2]);
				RedisObject obj = new RedisObject(ObjectType.STRING, tokens[3]);
				obj.setExpireAt(System.currentTimeMillis() + (seconds * 1000));
				expManager.updateExpiry(tokens[1], (seconds * 1000));
				dataStore.put(tokens[1], obj);
				out.println("OK");
			} catch (NumberFormatException e) {
				out.println("(error) ERR value is not an integer or out of range");
			}
		}
	}

}
