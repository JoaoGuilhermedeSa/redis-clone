package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class HashDelHandler implements EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
		if (tokens.length < 3) {
			out.println("(error) ERR wrong number of arguments for 'hdel' command");
			return;
		}
		RedisObject obj = redisStoreService.get(tokens[1]);
		if (obj == null) {
			out.println("(integer) 0");
			return;
		}
		if (obj.getType() != ObjectType.HASH) {
			out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		java.util.Map<String, String> hash = obj.getValue();
		int deletedFields = 0;
		for (int i = 2; i < tokens.length; i++) {
			if (hash.remove(tokens[i]) != null) {
				deletedFields++;
			}
		}
		out.println("(integer) " + deletedFields);
	}

}
