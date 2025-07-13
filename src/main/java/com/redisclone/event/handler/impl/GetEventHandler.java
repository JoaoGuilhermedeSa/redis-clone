package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class GetEventHandler implements EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
		if (tokens.length == 2) {
			String key = tokens[1];
			RedisObject obj = redisStoreService.get(key);
			if (obj != null && obj.isExpired()) {
				out.println("(nil)");
			} else if (obj != null) {
				if (obj.getType() == ObjectType.STRING) {
					out.println("\"" + obj.getValue() + "\"");
				} else {
					out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
				}
			} else {
				out.println("(nil)");
			}
		} else {
			out.println("(error) ERR wrong number of arguments for 'get' command");
		}
	}

}