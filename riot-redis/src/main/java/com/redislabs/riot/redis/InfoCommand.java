package com.redislabs.riot.redis;

import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.api.sync.RedisServerCommands;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "info", aliases = {"i"}, description = "Display INFO command output")
public class InfoCommand extends AbstractRedisCommand {

    @Override
    @SuppressWarnings("unchecked")
    protected void execute(BaseRedisCommands<String, String> commands) {
        log.info(((RedisServerCommands<String, String>) commands).info());
    }
}
