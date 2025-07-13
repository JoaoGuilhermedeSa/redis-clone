package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class TtlEventHandler implements EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens,
			PrintWriter out) {
		if (tokens.length == 2) {
			String key = tokens[1];
			RedisObject obj = redisStoreService.get(key);
			if (obj == null) {
				out.println("(integer) -2");
			} else if (obj.isExpired()) {
				redisStoreService.remove(key);
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