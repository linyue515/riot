package com.redislabs.riot.test;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

@Testcontainers
@SuppressWarnings("rawtypes")
public abstract class BaseTest {

	private final static String COMMAND_PREAMBLE = "❯";
	protected final static String LOCALHOST = "localhost";
	private static final int REDIS_PORT = 6379;
	private static final DockerImageName DOCKER_IMAGE_NAME = DockerImageName.parse("redislabs/redisearch:latest");

	private RedisClient client;

	@Container
	protected static final GenericContainer redis = redisContainer();

	@SuppressWarnings("resource")
	protected static GenericContainer redisContainer() {
		return new GenericContainer(DOCKER_IMAGE_NAME).withExposedPorts(REDIS_PORT);
	}

	@BeforeEach
	public void setup() {
		client = RedisClient.create(RedisURI.create(redis.getHost(), redis.getFirstMappedPort()));
		client.connect().sync().flushall();
	}

	protected StatefulRedisConnection<String, String> connection() {
		return client.connect();
	}

	protected RedisCommands<String, String> commands() {
		return client.connect().sync();
	}

	@AfterEach
	public void teardown() {
		if (client != null) {
			client.shutdown();
		}
	}

	protected String process(String command) {
		return connectionArgs(redis) + removeConnectionArgs(command);
	}

	private String removeConnectionArgs(String command) {
		return command.replace("-h localhost -p 6379", "");
	}

	protected String connectionArgs(GenericContainer redis) {
		return "-h " + redis.getHost() + " -p " + redis.getFirstMappedPort();
	}

	protected int executeFile(String filename) throws Exception {
		return execute(args(filename));
	}

	protected String[] args(String filename) throws Exception {
		try (InputStream inputStream = getClass().getResourceAsStream(filename)) {
			String command = IOUtils.toString(inputStream, Charset.defaultCharset());
			return CommandLineUtils.translateCommandline(process(removePreamble(command)));
		}
	}

	protected RedisURI redisURI(GenericContainer redis) {
		return RedisURI.create(redis.getHost(), redis.getFirstMappedPort());
	}

	protected abstract int execute(String[] args) throws Exception;

	protected String commandPrefix() {
		return COMMAND_PREAMBLE + " " + applicationName();
	}

	protected abstract String applicationName();

	private String removePreamble(String command) {
		if (command.startsWith(commandPrefix())) {
			return command.substring(commandPrefix().length());
		}
		return command;
	}

}
