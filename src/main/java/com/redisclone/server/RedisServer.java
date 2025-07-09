package com.redisclone.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redisclone.client.ClientHandler;
import com.redisclone.model.RedisObject;

public class RedisServer {

    private static final Logger logger = LoggerFactory.getLogger(RedisServer.class);
    private static final int PORT = 6379;
    private static final ConcurrentHashMap<String, RedisObject> dataStore = new ConcurrentHashMap<>();

    static {
        Thread cleanerThread = new Thread(() -> {
            while (true) {
                for (String key : dataStore.keySet()) {
                    if (dataStore.get(key).isExpired()) {
                        dataStore.remove(key);
                    }
                }
                try {
                    Thread.sleep(1000); // Check every second
                } catch (InterruptedException e) {
                    logger.error("Cleaner thread interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanerThread.setDaemon(true);
        cleanerThread.start();
    }

    public void start() {
        ExecutorService executor = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Redis clone server started on port {}", PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket, dataStore));
            }
        } catch (IOException e) {
            logger.error("Error starting server", e);
        }
    }
}
