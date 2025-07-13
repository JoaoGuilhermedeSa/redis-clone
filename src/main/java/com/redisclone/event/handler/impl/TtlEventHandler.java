package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.RedisObject;

public class TtlEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens,
			PrintWriter out) {
		if (tokens.length == 2) {
			String key = tokens[1];
			RedisObject obj = dataStore.get(key);
			if (obj == null) {
				out.println("(integer) -2");
			} else if (obj.isExpired()) {
				dataStore.remove(key);
				expManager.removeExpiry(key);
				out.println("(integer) -2");
			} else if (obj.getExpireAt() == -1) {
				out.println("(integer) -1");
			} else {
				long ttl = (obj.getExpireAt() - System.currentTimeMillis()) / 1000;
				out.println("(integer) " + ttl);
			}
		} else {
			out.println("(error) ERR wrong number of arguments for 'ttl' command");
		}
	}

}