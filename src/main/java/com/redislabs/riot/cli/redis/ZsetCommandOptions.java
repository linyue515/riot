package com.redislabs.riot.cli.redis;

import com.redislabs.riot.batch.redis.writer.ZaddMapWriter;

import lombok.Data;
import picocli.CommandLine.Option;

public @Data class ZsetCommandOptions {

	@Option(names = "--zset-score", description = "Field to use for sorted set scores", paramLabel = "<field>")
	private String zsetScore;
	@Option(names = "--zset-default", description = "Score when field not present (default: ${DEFAULT-VALUE})", paramLabel = "<float>")
	private double zsetDefaultScore = 1d;

	public ZaddMapWriter writer() {
		ZaddMapWriter writer = new ZaddMapWriter();
		writer.setDefaultScore(zsetDefaultScore);
		writer.setScoreField(zsetScore);
		return writer;
	}
}