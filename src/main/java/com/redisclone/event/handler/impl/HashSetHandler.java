package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class HashSetHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens,
			PrintWriter out) {
		if (tokens.length < 4 || tokens.length % 2 != 0) {
			out.println("(error) ERR wrong number of arguments for 'hset' command");
			return;
		}
		RedisObject obj = dataStore.get(tokens[1]);
		if (obj != null && obj.getType() != ObjectType.HASH) {
			out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		if (obj == null) {
			obj = new RedisObject(ObjectType.HASH, new java.util.concurrent.ConcurrentHashMap<String, String>());
			dataStore.put(tokens[1], obj);
		}
		java.util.Map<String, String> hash = obj.getValue();
		int newFields = 0;
		for (int i = 2; i < tokens.length; i += 2) {
			if (hash.put(tokens[i], tokens[i + 1]) == null) {
				newFields++;
			}
		}
		out.println("(integer) " + newFields);
	}

}
