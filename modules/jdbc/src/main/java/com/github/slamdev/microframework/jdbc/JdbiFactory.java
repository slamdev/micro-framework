package com.github.slamdev.microframework.jdbc;

import lombok.experimental.UtilityClass;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;

import javax.sql.DataSource;

@UtilityClass
public class JdbiFactory {

    public static IDBI create(DataSource dataSource) {
        return new DBI(dataSource);
    }
}
