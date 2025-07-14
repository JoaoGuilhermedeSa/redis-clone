package com.redisclone.event.handler;

import java.io.PrintWriter;

import com.redisclone.service.RedisStoreService;

public abstract class AbstractEventHandler {

	public abstract void handle(RedisStoreService redisStoreService, String[] tokens, PrintWriter out);
	public void sendBulkString(PrintWriter out, String value) {
        if (value == null) {
            out.print("$-1\r\n");
        } else {
            out.print("$" + value.length() + "\r\n" + value + "\r\n");
        }
    }

    public void sendEmptyArray(PrintWriter out) {
        out.print("*0\r\n");
    }

    public void sendNullBulkString(PrintWriter out) {
        out.print("$-1\r\n");
    }
    
    public void sendSimpleString(PrintWriter out, String value) {
        out.print("+" + value + "\r\n");
    }

    public void sendError(PrintWriter out, String message) {
        out.print("-" + message + "\r\n");
    }
    
    public void sendSimpleInteger(PrintWriter out, int value) {
        out.print(":" + value + "\r\n");
    }

}