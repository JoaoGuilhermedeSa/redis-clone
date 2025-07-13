package com.redisclone.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redisclone.client.ClientHandler;
import com.redisclone.config.PropertiesLoader;
import com.redisclone.service.RedisStoreService;

public class RedisServer {

	private static final Logger logger = LoggerFactory.getLogger(RedisServer.class);
	private final RedisStoreService redisStoreService = new RedisStoreService();

	private volatile ServerSocket serverSocket;

	public void start() {
		PropertiesLoader propsLoader = new PropertiesLoader();

		ExecutorService executor = Executors.newFixedThreadPool(propsLoader.getMaxThreads());

		addShutdownHook(executor);

		try {
			int port = propsLoader.getPort();
			String bindAddress = propsLoader.getBindAddress();
			serverSocket = new ServerSocket(port, 50, InetAddress.getByName(bindAddress));
			logger.info("Redis clone server started on port {} bound to address {}", port, bindAddress);

			while (true) {
				Socket clientSocket = serverSocket.accept();
				logger.debug("Accepted connection from {}", clientSocket.getInetAddress());
				executor.submit(new ClientHandler(clientSocket, redisStoreService));
			}
		} catch (IOException e) {
			if (serverSocket != null && serverSocket.isClosed()) {
				logger.info("Server stopped gracefully");
			} else {
				logger.error("Error in server loop", e);
			}
		}
	}

	private void addShutdownHook(ExecutorService executor) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Initiating graceful shutdown...");
			executor.shutdown();
			redisStoreService.shutdownCleanerThread();
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