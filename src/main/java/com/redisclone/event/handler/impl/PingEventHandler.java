package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.RedisObject;

public class PingEventHandler implements EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens,
			PrintWriter out) {
		out.println("PONG");
	}

}
