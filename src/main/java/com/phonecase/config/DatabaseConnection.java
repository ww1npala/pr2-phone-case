package com.phonecase.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Properties;

/**
 * Управляє пулом з'єднань до бази даних SQLite.
 * Реалізує патерни Singleton та Connection Pool.
 * Використовується через Guice DI-контейнер.
 */

@Singleton
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final int POOL_SIZE = 5;

    private final String jdbcUrl;
    private final Deque<Connection> pool = new ArrayDeque<>();

    public DatabaseConnection() {
        Properties props = loadProperties();
        this.jdbcUrl = props.getProperty("db.url", "jdbc:sqlite:phonecase.db");
        logger.info("Ініціалізація пулу з'єднань: {}", jdbcUrl);
        try {
            Class.forName("org.sqlite.JDBC");
            for (int i = 0; i < POOL_SIZE; i++) {
                pool.push(createConnection());
            }
            logger.info("Пул з'єднань створено ({} з'єднань).", POOL_SIZE);
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Помилка ініціалізації пулу: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалось ініціалізувати пул з'єднань", e);
        }
    }


    public synchronized Connection getConnection() throws SQLException {
        if (pool.isEmpty()) {
            logger.warn("Пул вичерпано — створюємо нове з'єднання.");
            return createConnection();
        }
        Connection conn = pool.pop();
        if (conn.isClosed()) {
            conn = createConnection();
        }
        return conn;
    }


    public synchronized void releaseConnection(Connection connection) {
        if (connection != null) {
            pool.push(connection);
        }
    }

    private Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcUrl);
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        return conn;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            logger.warn("application.properties не знайдено, використовуємо defaults.");
        }
        return props;
    }
}