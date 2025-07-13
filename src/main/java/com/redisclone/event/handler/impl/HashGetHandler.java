package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class HashGetHandler implements EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens,
			PrintWriter out) {
		if (tokens.length != 3) {
			out.println("(error) ERR wrong number of arguments for 'hget' command");
			return;
		}
		RedisObject obj = redisStoreService.get(tokens[1]);
		if (obj == null) {
			out.println("(nil)");
			return;
		}
		if (obj.getType() != ObjectType.HASH) {
			out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		java.util.Map<String, String> hash = obj.getValue();
		String value = hash.get(tokens[2]);
		if (value != null) {
			out.println("\"" + value + "\"");
		} else {
			out.println("(nil)");
		}
	}

}
