package com.redislabs.riot.cli.db;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import com.zaxxer.hikari.HikariDataSource;

import picocli.CommandLine.Option;

public class DatabaseOptions {

	private final Logger log = LoggerFactory.getLogger(DatabaseOptions.class);

	@Option(names = { "-d",
			"--driver" }, description = "Fully qualified name of the JDBC driver", paramLabel = "<class>")
	private String driver;
	@Option(names = { "-u",
			"--url" }, required = true, description = "URL to connect to the database", paramLabel = "<string>")
	private String url;
	@Option(names = { "-n", "--username" }, description = "Login username of the database", paramLabel = "<string>")
	private String username;
	@Option(names = "--password", arity = "0..1", interactive = true, description = "Login password of the database", paramLabel = "<pwd>")
	private String password;

	public DataSource dataSource() {
		DataSourceProperties properties = new DataSourceProperties();
		properties.setUrl(url);
		properties.setDriverClassName(driver);
		properties.setUsername(username);
		properties.setPassword(password);
		log.debug("Initializing datasource: driver={} url={}", driver, url);
		return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

}