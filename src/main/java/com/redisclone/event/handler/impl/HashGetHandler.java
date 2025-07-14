package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class HashGetHandler extends AbstractEventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens,
			PrintWriter out) {
		if (tokens.length != 3) {
			sendError(out, "ERR wrong number of arguments for 'hget' command");
			return;
		}
		RedisObject obj = redisStoreService.get(tokens[1]);
		if (obj == null) {
			sendNullBulkString(out);
			return;
		}
		if (obj.getType() != ObjectType.HASH) {
			sendError(out, "WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		java.util.Map<String, String> hash = obj.getValue();
		String value = hash.get(tokens[2]);
		if (value != null) {
			sendBulkString(out, value);
		} else {
			sendNullBulkString(out);
		}
	}

}
