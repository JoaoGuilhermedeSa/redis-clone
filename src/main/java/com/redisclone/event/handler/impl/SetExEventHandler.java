package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class SetExEventHandler implements EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
		if (tokens.length == 4) {
			try {
				long seconds = Long.parseLong(tokens[2]);
				RedisObject obj = new RedisObject(ObjectType.STRING, tokens[3]);
				obj.setExpireAt(System.currentTimeMillis() + (seconds * 1000));
				redisStoreService.updateExpiry(tokens[1], (seconds * 1000));
				redisStoreService.put(tokens[1], obj);
				out.println("OK");
			} catch (NumberFormatException e) {
				out.println("(error) ERR value is not an integer or out of range");
			}
		}
	}

}
