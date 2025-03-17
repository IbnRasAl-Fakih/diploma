package com.diploma.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseConnectionPoolService {

    private final Map<String, Connection> connectionPool = new ConcurrentHashMap<>();

    public void addConnection(String sessionId, Connection connection) {
        connectionPool.put(sessionId, connection);
    }

    public Connection getConnection(String sessionId) {
        return connectionPool.get(sessionId);
    }

    public void removeConnection(String sessionId) {
        Connection connection = connectionPool.remove(sessionId);
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Соединение закрыто для сессии: " + sessionId);
            } catch (Exception e) {
                System.err.println("❌ Ошибка при закрытии соединения: " + e.getMessage());
            }
        }
    }

    public boolean hasConnection(String sessionId) {
        return connectionPool.containsKey(sessionId);
    }
}
