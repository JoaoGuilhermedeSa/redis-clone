package com.redisclone.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redisclone.event.handler.AbstractEventHandler;
import com.redisclone.event.handler.impl.DelEventHandler;
import com.redisclone.event.handler.impl.EchoEventHandler;
import com.redisclone.event.handler.impl.ExpireEventHandler;
import com.redisclone.event.handler.impl.GetEventHandler;
import com.redisclone.event.handler.impl.HashDelHandler;
import com.redisclone.event.handler.impl.HashGetAllHandler;
import com.redisclone.event.handler.impl.HashGetHandler;
import com.redisclone.event.handler.impl.HashSetHandler;
import com.redisclone.event.handler.impl.ListPopLeftHandler;
import com.redisclone.event.handler.impl.ListPopRightHandler;
import com.redisclone.event.handler.impl.ListPushLeftHandler;
import com.redisclone.event.handler.impl.ListPushRightHandler;
import com.redisclone.event.handler.impl.ListRangeHandler;
import com.redisclone.event.handler.impl.PingEventHandler;
import com.redisclone.event.handler.impl.SetEventHandler;
import com.redisclone.event.handler.impl.SetExEventHandler;
import com.redisclone.event.handler.impl.TtlEventHandler;
import com.redisclone.model.EventEnum;
import com.redisclone.service.RedisStoreService;

public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final Map<EventEnum, AbstractEventHandler> eventRegistry;
    private final RedisStoreService redisStoreService;

    public ClientHandler(Socket socket, RedisStoreService redisStoreService) {
        this.clientSocket = socket;
        this.redisStoreService = redisStoreService;
        this.eventRegistry = new HashMap<>();
        this.eventRegistry.put(EventEnum.SET, new SetEventHandler());
        this.eventRegistry.put(EventEnum.GET, new GetEventHandler());
        this.eventRegistry.put(EventEnum.SETEX, new SetExEventHandler());
        this.eventRegistry.put(EventEnum.EXPIRE, new ExpireEventHandler());
        this.eventRegistry.put(EventEnum.TTL, new TtlEventHandler());
        this.eventRegistry.put(EventEnum.LPUSH, new ListPushLeftHandler());
        this.eventRegistry.put(EventEnum.RPUSH, new ListPushRightHandler());
        this.eventRegistry.put(EventEnum.LPOP, new ListPopLeftHandler());
        this.eventRegistry.put(EventEnum.RPOP, new ListPopRightHandler());
        this.eventRegistry.put(EventEnum.LRANGE, new ListRangeHandler());
        this.eventRegistry.put(EventEnum.HSET, new HashSetHandler());
        this.eventRegistry.put(EventEnum.HGET, new HashGetHandler());
        this.eventRegistry.put(EventEnum.HGETALL, new HashGetAllHandler());
        this.eventRegistry.put(EventEnum.HDEL, new HashDelHandler());
        this.eventRegistry.put(EventEnum.PING, new PingEventHandler());
        this.eventRegistry.put(EventEnum.ECHO, new EchoEventHandler());
        this.eventRegistry.put(EventEnum.DEL, new DelEventHandler());
    }

    @Override
    public void run() {
        try (InputStream inputStream = clientSocket.getInputStream();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            while (true) {
            	String[] tokens = new String[0];
                try {
                    tokens = readRespCommand(inputStream);
                    if (tokens.length == 0) {
                    	out.flush();
                        break; 
                    }
                    String command = tokens[0].toUpperCase();

                    Optional<EventEnum> eventOpt = Stream.of(EventEnum.values())
                            .filter(event -> event.name().equals(command)).findFirst();

                    logger.info("Received command: " + String.join(" ", tokens));
                        if (eventOpt.isPresent()) {
                            this.eventRegistry.get(eventOpt.get()).handle(redisStoreService, tokens, out);
                        } else {
                            sendError(out, "ERR unknown command '" + command + "'");
                        }
                        out.flush();  // Ensure flush after every command
                    } catch (Exception e) {
                        logger.error("Error handling command: " + String.join(" ", tokens), e);
                        sendError(out, "ERR internal server error: " + e.getMessage());
                        out.flush();
                    }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String[] readRespCommand(InputStream in) throws IOException {
        int byteRead = in.read();
        if (byteRead == -1) {
            return new String[0]; // End of stream
        }
        if ((char) byteRead != '*') {
            throw new IOException("Expected array (*) for command, got: " + (char) byteRead);
        }
        int arraySize = readInteger(in);
        if (arraySize < 1) {
            throw new IOException("Invalid command array size: " + arraySize);
        }
        List<String> tokens = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            tokens.add(readBulkString(in));
        }
        return tokens.toArray(new String[0]);
    }

    private String readBulkString(InputStream in) throws IOException {
        int byteRead = in.read();
        if ((char) byteRead != '$') {
            throw new IOException("Expected bulk string ($), got: " + (char) byteRead);
        }
        int length = readInteger(in);
        if (length == -1) {
            return null; // Null bulk string
        }
        if (length < 0) {
            throw new IOException("Invalid bulk string length: " + length);
        }
        byte[] data = new byte[length];
        int bytesRead = in.readNBytes(data, 0, length);
        if (bytesRead != length) {
            throw new IOException("Incomplete bulk string read: expected " + length + ", got " + bytesRead);
        }
        skipCrlf(in);
        return new String(data);
    }

    private int readInteger(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = in.read()) != -1 && ch != '\r') {
            sb.append((char) ch);
        }
        if (ch == -1) {
            throw new IOException("Unexpected end of stream while reading integer");
        }
        in.read(); // Skip \n
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid integer: " + sb, e);
        }
    }

    private void skipCrlf(InputStream in) throws IOException {
        if (in.read() != '\r' || in.read() != '\n') {
            throw new IOException("Expected CRLF");
        }
    }

    private void sendError(PrintWriter out, String message) {
        out.print("-" + message + "\r\n");
    }

}