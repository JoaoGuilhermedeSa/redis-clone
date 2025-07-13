package com.redisclone.event.handler;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.RedisObject;

public interface EventHandler {

	public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens,
			PrintWriter out);

}
