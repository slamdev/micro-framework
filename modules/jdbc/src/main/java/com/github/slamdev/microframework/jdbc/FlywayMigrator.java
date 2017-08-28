package com.github.slamdev.microframework.jdbc;

import lombok.experimental.UtilityClass;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

@UtilityClass
public class FlywayMigrator {

    public static void run(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();
    }
}
