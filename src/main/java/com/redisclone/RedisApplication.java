package com.redisclone;

import com.redisclone.server.RedisServer;

public class RedisApplication {

    public static void main(String[] args) {
        new RedisServer().start();
    }

}
