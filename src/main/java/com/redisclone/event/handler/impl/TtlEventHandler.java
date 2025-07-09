package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.RedisObject;

public class TtlEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, String[] tokens, PrintWriter out) {
        if (tokens.length == 2) {
            RedisObject obj = dataStore.get(tokens[1]);
            if (obj != null) {
                if (obj.isExpired()) {
                    out.println("(integer) -2");
                } else if (obj.getExpireAt() == -1) {
                    out.println("(integer) -1");
                } else {
                    long ttl = (obj.getExpireAt() - System.currentTimeMillis()) / 1000;
                    out.println("(integer) " + ttl);
                }
            } else {
                out.println("(integer) -2");
            }
        }
	}

}
