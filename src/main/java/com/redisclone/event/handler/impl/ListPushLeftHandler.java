package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class ListPushLeftHandler implements EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens,
			PrintWriter out) {
		if (tokens.length < 3) {
			out.println("(error) ERR wrong number of arguments for '" + tokens[0].toLowerCase() + "' command");
			return;
		}
		RedisObject obj = redisStoreService.get(tokens[1]);
		if (obj != null && obj.getType() != ObjectType.LIST) {
			out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		if (obj == null) {
			obj = new RedisObject(ObjectType.LIST, new java.util.LinkedList<String>());
			redisStoreService.put(tokens[1], obj);
		}
		java.util.List<String> list = obj.getValue();
		for (int i = 2; i < tokens.length; i++) {
			list.add(0, tokens[i]);
		}
		out.println("(integer) " + list.size());
	}

}
