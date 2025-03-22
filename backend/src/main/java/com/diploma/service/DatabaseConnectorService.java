package com.diploma.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

@Service
public class DatabaseConnectorService {

    private final DatabaseConnectionPoolService connectionPoolService;

    public DatabaseConnectorService(DatabaseConnectionPoolService connectionPoolService) {
        this.connectionPoolService = connectionPoolService;
    }

    public String connect(String url, String username, String password, String driver) throws Exception {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url, username, password);
        
        String sessionId = UUID.randomUUID().toString();
        
        connectionPoolService.addConnection(sessionId, connection);

        System.out.println("✅ Соединение успешно сохранено для сессии: " + sessionId);
        return sessionId;
    }
}
