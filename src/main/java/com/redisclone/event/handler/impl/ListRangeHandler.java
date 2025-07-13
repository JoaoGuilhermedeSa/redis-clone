package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class ListRangeHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens,
			PrintWriter out) {
		if (tokens.length != 4) {
			out.println("(error) ERR wrong number of arguments for 'lrange' command");
			return;
		}
		RedisObject obj = dataStore.get(tokens[1]);
		if (obj == null) {
			out.println("(empty list or set)");
			return;
		}
		if (obj.getType() != ObjectType.LIST) {
			out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
			return;
		}
		java.util.List<String> list = obj.getValue();
		try {
			int start = Integer.parseInt(tokens[2]);
			int end = Integer.parseInt(tokens[3]);
			if (start < 0)
				start = list.size() + start;
			if (end < 0)
				end = list.size() + end;
			if (start < 0)
				start = 0;
			if (end >= list.size())
				end = list.size() - 1;

			if (start > end) {
				out.println("(empty list or set)");
				return;
			}

			for (int i = start; i <= end; i++) {
				out.println((i - start + 1) + ") \"" + list.get(i) + "\"");
			}
		} catch (NumberFormatException e) {
			out.println("(error) ERR value is not an integer or out of range");
		}
	}

}
