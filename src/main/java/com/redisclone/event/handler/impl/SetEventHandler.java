package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class SetEventHandler extends AbstractEventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens,
			PrintWriter out) {
		if (tokens.length == 3) {
			redisStoreService.put(tokens[1], new RedisObject(ObjectType.STRING, tokens[2]));
			sendSimpleString(out, "OK");
		} else {
			sendError(out, "ERR wrong number of arguments for 'set' command");
		}
	}

}
