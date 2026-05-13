package com.scapelike.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
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
        this.dsl = DSL.using(pool, SQLDialect.MARIADB, new Settings().withRenderNameCase(RenderNameCase.LOWER));
        migrate();
        log.info("Database connected");
    }

    private void migrate() {
        try (InputStream stream = getClass().getResourceAsStream("/schema.sql")) {
            if (stream == null) {
                throw new RuntimeException("schema.sql not found on classpath");
            }
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conn = pool.getConnection();
                    Statement stmt = conn.createStatement()) {
                // TODO: replace with a proper parser when migrations include semicolons in string literals
                for (String statement : sql.split(";")) {
                    String trimmed = statement.strip();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Migration failed", e);
        }
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
