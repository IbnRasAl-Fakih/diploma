package com.diploma.service.DbToolsService;

import org.springframework.stereotype.Service;

import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_connector")
public class DatabaseConnectorService implements NodeExecutor{

    private final DatabaseConnectionPoolService connectionPoolService;

    public DatabaseConnectorService(DatabaseConnectionPoolService connectionPoolService) {
        this.connectionPoolService = connectionPoolService;
    }

    @Override
    public Object execute(Map<String, Object> fields, List<String> inputs) {
        try {
            String url = (String) fields.get("url");
            String username = (String) fields.get("username");
            String password = (String) fields.get("password");
            String driver = (String) fields.get("driver");

            return connect(url, username, password, driver);
        } catch (Exception e) {
            System.err.println("Error in execute(): " + e.getMessage());
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    public Map<String, String> connect(String url, String username, String password, String driver) throws Exception {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url, username, password);
        
        String sessionId = UUID.randomUUID().toString();
        
        connectionPoolService.addConnection(sessionId, connection);
        
        return Map.of("sessionId", sessionId);
    }
}