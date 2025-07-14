package com.redisclone.event.handler.impl;

import java.io.PrintWriter;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;
import com.redisclone.service.RedisStoreService;

public class ListRangeHandler extends AbstractEventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens,
			PrintWriter out) {
		if (tokens.length != 4) {
			sendError(out, "ERR wrong number of arguments for 'lrange' command");
			return;
		}
		RedisObject obj = redisStoreService.get(tokens[1]);
		if (obj == null) {
			sendEmptyArray(out);
			return;
		}
		if (obj.getType() != ObjectType.LIST) {
			sendError(out, "WRONGTYPE Operation against a key holding the wrong kind of value");
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
				sendEmptyArray(out);
				return;
			}

			for (int i = start; i <= end; i++) {
				out.println((i - start + 1) + ") \"" + list.get(i) + "\"");
			}
		} catch (NumberFormatException e) {
			sendError(out, "ERR value is not an integer or out of range");
		}
	}

}
