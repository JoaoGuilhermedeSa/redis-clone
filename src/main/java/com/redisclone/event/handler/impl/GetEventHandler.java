package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class GetEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, String[] tokens, PrintWriter out) {
		if (tokens.length == 2) {
			RedisObject obj = dataStore.get(tokens[1]);
			if (obj != null && !obj.isExpired()) {
				if (obj.getType() == ObjectType.STRING) {
					out.println("\"" + obj.getValue() + "\"");
				} else {
					out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
				}
			} else {
				out.println("(nil)");
			}
		}
	}

}
