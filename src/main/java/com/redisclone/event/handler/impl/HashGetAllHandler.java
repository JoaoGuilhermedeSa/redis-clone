package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class HashGetAllHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, String[] tokens, PrintWriter out) {
        if (tokens.length != 2) {
            out.println("(error) ERR wrong number of arguments for 'hgetall' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj == null) {
            out.println("(empty list or set)");
            return;
        }
        if (obj.getType() != ObjectType.HASH) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        java.util.Map<String, String> hash = obj.getValue();
        int i = 1;
        for (java.util.Map.Entry<String, String> entry : hash.entrySet()) {
            out.println(i++ + ") \"" + entry.getKey() + "\"");
            out.println(i++ + ") \"" + entry.getValue() + "\"");
        }
	}

}
