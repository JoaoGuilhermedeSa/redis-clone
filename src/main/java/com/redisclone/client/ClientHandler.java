package com.redisclone.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redisclone.event.handler.EventHandler;
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
import com.redisclone.model.RedisObject;

public class ClientHandler implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
	private final Socket clientSocket;
	private final ConcurrentHashMap<String, RedisObject> dataStore;
	private final Map<EventEnum, EventHandler> eventRegistry;

	public ClientHandler(Socket socket, ConcurrentHashMap<String, RedisObject> dataStore) {
		this.clientSocket = socket;
		this.dataStore = dataStore;
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
	}

	@Override
	public void run() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				String[] tokens = inputLine.split("\\s+");
				String command = tokens[0].toUpperCase();

				Optional<EventEnum> eventOpt = Stream.of(EventEnum.values())
						.filter(event -> event.name().equals(command)).findFirst();

				if (eventOpt.isPresent()) {
					this.eventRegistry.get(eventOpt.get()).handle(dataStore, tokens, out);
				} else {
					out.println("(error) ERR unknown command '" + command + "'");
				}

			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
