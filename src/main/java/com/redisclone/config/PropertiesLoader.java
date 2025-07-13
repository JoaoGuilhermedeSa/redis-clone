package com.redisclone.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redisclone.server.RedisServer;

public class PropertiesLoader {

	private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);
	private static final String PORT = "server.port";
	private static final String BIND_ADDRESS = "server.bind.address";
	private static final String THREAD_POOL_SIZE = "thread.pool.size";
	private static final String CLEANER_THREAD_INTERVAL = "cleaner.interval.ms";

	private Properties props;

	public PropertiesLoader() {
		this.props = loadProperties();
	}

	public int getPort() {
		return Integer.parseInt(props.getProperty(PORT, "6379"));
	}

	public String getBindAddress() {
		return props.getProperty(BIND_ADDRESS, "127.0.0.1");
	}

	public int getMaxThreads() {
		return Integer.parseInt(
				props.getProperty(THREAD_POOL_SIZE, String.valueOf(Runtime.getRuntime().availableProcessors() * 2)));
	}

	public long getCleanerIntervalMs() {
		return Long.parseLong(props.getProperty(CLEANER_THREAD_INTERVAL, "1000"));
	}

	private Properties loadProperties() {
		Properties props = new Properties();
		try (InputStream is = RedisServer.class.getResourceAsStream("/config.properties")) {
			if (is != null) {
				props.load(is);
				logger.info("Loaded configuration from config.properties");
			} else {
				logger.info("No config.properties found, using defaults.");
			}
		} catch (IOException e) {
			logger.warn("Failed to load config.properties", e);
		}
		return props;
	}

}
