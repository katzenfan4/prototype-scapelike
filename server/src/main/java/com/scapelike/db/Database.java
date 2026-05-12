package com.scapelike.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private final HikariDataSource pool;

    public Database() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(
                "jdbc:mariadb://%s:%s/%s",
                env("DB_HOST", "localhost"), env("DB_PORT", "3306"), env("DB_NAME", "scapelike")));
        config.setUsername(env("DB_USER", "scapelike"));
        config.setPassword(env("DB_PASS", "scapelike"));
        config.setMaximumPoolSize(10);
        this.pool = new HikariDataSource(config);
        migrate();
        log.info("Database connected");
    }

    private void migrate() {
        try (Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(
                    """
                CREATE TABLE IF NOT EXISTS players (
                    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username   VARCHAR(32) NOT NULL UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Migration failed", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    private static String env(String key, String fallback) {
        String val = System.getenv(key);
        return val != null ? val : fallback;
    }
}
