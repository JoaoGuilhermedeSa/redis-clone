package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class ListPopRightHandler extends AbstractEventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out) {
		if (tokens.length != 2) {
			sendError(out, "ERR wrong number of arguments for '" + tokens[0].toLowerCase() + "' command");
			return;
		}
		RedisObject obj = redisStoreService.get(tokens[1]);
		if (obj == null) {
			sendNullBulkString(out);
			return;
		}
		if (obj.getType() != ObjectType.LIST) {
			sendError(out, "WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		java.util.List<String> list = obj.getValue();
		if (list.isEmpty()) {
			sendNullBulkString(out);
		} else {
			String value = list.remove(list.size() - 1);
			sendBulkString(out, value);
		}
	}

}
