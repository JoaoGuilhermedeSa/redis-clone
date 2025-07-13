package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class ListPopLeftHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens, PrintWriter out) {
		if (tokens.length != 2) {
			out.println("(error) ERR wrong number of arguments for '" + tokens[0].toLowerCase() + "' command");
			return;
		}
		RedisObject obj = dataStore.get(tokens[1]);
		if (obj == null) {
			out.println("(nil)");
			return;
		}
		if (obj.getType() != ObjectType.LIST) {
			out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		java.util.List<String> list = obj.getValue();
		if (list.isEmpty()) {
			out.println("(nil)");
		} else {
			String value = list.remove(0);
			out.println("\"" + value + "\"");
		}
	}

}
