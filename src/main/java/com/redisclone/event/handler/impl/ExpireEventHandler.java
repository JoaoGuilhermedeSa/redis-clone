package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.model.RedisObject;

public class ExpireEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, String[] tokens, PrintWriter out) {
        if (tokens.length == 3) {
            RedisObject obj = dataStore.get(tokens[1]);
            if (obj != null) {
                try {
                    long seconds = Long.parseLong(tokens[2]);
                    obj.setExpireAt(System.currentTimeMillis() + seconds * 1000);
                    out.println("(integer) 1");
                } catch (NumberFormatException e) {
                    out.println("(error) ERR value is not an integer or out of range");
                }
            } else {
                out.println("(integer) 0");
            }
        } else {
            out.println("(error) ERR wrong number of arguments for 'expire' command");
        }
	}

}
