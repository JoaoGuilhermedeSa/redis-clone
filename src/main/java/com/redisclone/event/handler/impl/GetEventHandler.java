package com.redisclone.event.handler.impl;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.event.handler.EventHandler;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class GetEventHandler implements EventHandler {

    public void handle(ConcurrentHashMap<String, RedisObject> dataStore, ExpirationManager expManager, String[] tokens, PrintWriter out) {
        if (tokens.length == 2) {
            String key = tokens[1];
            RedisObject obj = dataStore.get(key);
            if (obj != null && obj.isExpired()) {
                dataStore.remove(key);
                expManager.removeExpiry(key);
                out.println("(nil)");
            } else if (obj != null) {
                if (obj.getType() == ObjectType.STRING) {
                    out.println("\"" + obj.getValue() + "\"");
                } else {
                    out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
                }
            } else {
                out.println("(nil)");
            }
        } else {
            out.println("(error) ERR wrong number of arguments for 'get' command");
        }
    }

}