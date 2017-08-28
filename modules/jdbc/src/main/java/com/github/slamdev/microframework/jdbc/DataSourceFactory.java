package com.github.slamdev.microframework.jdbc;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;

import javax.sql.DataSource;

@UtilityClass
public class DataSourceFactory {

    public static DataSource create(Config config) {
        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setJdbcUrl(config.getString("datasource.url"));
        dataSourceConfig.setUsername(config.getString("datasource.username"));
        dataSourceConfig.setPassword(config.getString("datasource.password"));
        return new HikariDataSource(dataSourceConfig);
    }
}
