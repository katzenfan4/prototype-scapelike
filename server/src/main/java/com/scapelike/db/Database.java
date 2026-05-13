package com.scapelike.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private final HikariDataSource pool;
    private final DSLContext dsl;

    public Database() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(
                "jdbc:mariadb://%s:%s/%s",
                env("DB_HOST", "localhost"), env("DB_PORT", "3306"), env("DB_NAME", "scapelike")));
        config.setUsername(env("DB_USER", "scapelike"));
        config.setPassword(env("DB_PASS", "scapelike"));
        config.setMaximumPoolSize(10);
        this.pool = new HikariDataSource(config);
        this.dsl = DSL.using(pool, SQLDialect.MARIADB);
        migrate();
        log.info("Database connected");
    }

    private void migrate() {
        // TODO: add schema migrations
    }

    public DSLContext dsl() {
        return dsl;
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value != null ? value : fallback;
    }
}
