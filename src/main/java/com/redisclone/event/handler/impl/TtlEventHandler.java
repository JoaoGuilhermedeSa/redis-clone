package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class TtlEventHandler extends AbstractEventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens,
			PrintWriter out) {
		if (tokens.length == 2) {
			String key = tokens[1];
			RedisObject obj = redisStoreService.get(key);
			if (obj == null) {
				sendSimpleInteger(out, -2);
			} else if (obj.isExpired()) {
				redisStoreService.remove(key);
				sendSimpleInteger(out, -2);
			} else if (obj.getExpireAt() == -1) {
				sendSimpleInteger(out, -1);
			} else {
				long ttl = (obj.getExpireAt() - System.currentTimeMillis()) / 1000;
				sendSimpleInteger(out, -1);
			}
		} else {
			sendError(out, "ERR wrong number of arguments for 'ttl' command");
		}
	}

}