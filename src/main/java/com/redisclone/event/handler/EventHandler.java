package com.redisclone.event.handler;

import java.io.PrintWriter;

import com.redisclone.service.RedisStoreService;

public interface EventHandler {

	public void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out);

}