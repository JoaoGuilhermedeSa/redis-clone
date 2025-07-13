package com.redisclone.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redisclone.client.ClientHandler;
import com.redisclone.config.PropertiesLoader;
import com.redisclone.manager.ExpirationManager;
import com.redisclone.model.ExpirableEntry;
import com.redisclone.model.RedisObject;

public class RedisServer {

    private static final Logger logger = LoggerFactory.getLogger(RedisServer.class);
    private final ConcurrentHashMap<String, RedisObject> dataStore = new ConcurrentHashMap<>();
    private final ExpirationManager expManager = new ExpirationManager();

    private volatile ServerSocket serverSocket;

    public void start() {
        PropertiesLoader propsLoader = new PropertiesLoader();

        ExecutorService executor = Executors.newFixedThreadPool(propsLoader.getMaxThreads());

        Thread cleanerThread = createCleanerThread();

        addShutdownHook(cleanerThread, executor);

        try {
            int port = propsLoader.getPort();
            String bindAddress = propsLoader.getBindAddress();
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(bindAddress));
            logger.info("Redis clone server started on port {} bound to address {}", port, bindAddress);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.debug("Accepted connection from {}", clientSocket.getInetAddress());
                executor.submit(new ClientHandler(clientSocket, dataStore, expManager));
            }
        } catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed()) {
                logger.info("Server stopped gracefully");
            } else {
                logger.error("Error in server loop", e);
            }
        }
    }

    private Thread createCleanerThread() {
        Thread cleanerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ExpirableEntry entry = expManager.getExpiryQueue().take();
                    String key = entry.getKey();
                    ExpirableEntry current = expManager.getExpiryMap().get(key);
                    if (current == entry) {
                    	expManager.getExpiryMap().remove(key);
                        dataStore.remove(key);
                        logger.debug("Expired and removed key: {}", key);
                    }
                } catch (InterruptedException e) {
                    logger.info("Cleaner thread interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanerThread.setDaemon(true);
        cleanerThread.start();
        return cleanerThread;
    }

    private void addShutdownHook(Thread cleanerThread, ExecutorService executor) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Initiating graceful shutdown...");
            cleanerThread.interrupt();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("Executor did not terminate in time, forcing shutdown");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    logger.info("Server socket closed");
                }
            } catch (IOException e) {
                logger.error("Error closing server socket during shutdown", e);
            }
            logger.info("Shutdown complete.");
        }));

    }
}