package com.redis.riot.redis;

import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.riot.Transfer;
import com.redislabs.riot.redis.ReplicateCommand;
import com.redislabs.riot.redis.RiotRedis;
import com.redislabs.riot.test.BaseTest;
import com.redislabs.riot.test.DataPopulator;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.redis.support.KeyDump;
import org.springframework.batch.item.redis.support.LiveKeyItemReader;
import org.springframework.batch.item.redis.support.RedisItemReader;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import picocli.CommandLine;

@SuppressWarnings("rawtypes")
public class TestReplicate extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(TestReplicate.class);

    @Container
    private final GenericContainer targetRedis = redisContainer();

    @Override
    protected int execute(String[] args) {
        return new RiotRedis().execute(args);
    }

    @Override
    protected String applicationName() {
        return "riot-redis";
    }

    @Test
    public void replicate() throws Exception {
        RedisURI targetRedisURI = redisURI(targetRedis);
        RedisClient targetClient = RedisClient.create(targetRedisURI);
        targetClient.connect().sync().flushall();
        DataPopulator.builder().connection(connection()).build().run();
        Long sourceSize = commands().dbsize();
        Assertions.assertTrue(sourceSize > 0);
        runFile("/replicate.txt", originalTargetRedisURI(), targetRedisURI);
        Assertions.assertEquals(sourceSize, targetClient.connect().sync().dbsize());
    }

    private RedisURI originalTargetRedisURI() {
        return RedisURI.create(LOCALHOST, 6380);
    }

    @Test
    public void replicateLive() throws Exception {
        StatefulRediSearchConnection<String, String> connection = connection();
        connection.sync().configSet("notify-keyspace-events", "AK");
        connection.close();
        RedisURI targetRedisURI = redisURI(targetRedis);
        RedisClient targetClient = RedisClient.create(targetRedisURI);
        StatefulRedisConnection<String, String> targetConnection = targetClient.connect();
        targetConnection.sync().flushall();
        DataPopulator.builder().connection(connection()).build().run();
        String[] commandArgs = fileCommandArgs("/replicate-live.txt", originalTargetRedisURI(), targetRedisURI);
        RiotRedis riotRedis = new RiotRedis();
        CommandLine commandLine = riotRedis.commandLine();
        CommandLine.ParseResult parseResult = commandLine.parseArgs(commandArgs);
        ReplicateCommand command = parseResult.asCommandLineList().get(1).getCommand();
        Transfer<KeyDump<String>, KeyDump<String>> transfer = command.transfer();
        new Thread(() -> command.execute(transfer)).start();
        Thread.sleep(500);
        RedisCommands<String, String> commands = commands();
        int count = 39;
        for (int index = 0; index < count; index++) {
            commands.set("livestring:" + index, "value" + index);
            Thread.sleep(1);
        }
        Thread.sleep(500);
        RedisItemReader<String, KeyDump<String>> reader = (RedisItemReader<String, KeyDump<String>>) transfer.getReader();
        LiveKeyItemReader<String, String> keyReader = (LiveKeyItemReader<String, String>) reader.getKeyReader();
        log.info("Stopping LiveKeyItemReader");
        keyReader.stop();
        Long sourceSize = commands.dbsize();
        Assertions.assertTrue(sourceSize > 0);
        Long targetSize = targetConnection.sync().dbsize();
        Assertions.assertEquals(sourceSize, targetSize);
        targetConnection.close();
    }
}