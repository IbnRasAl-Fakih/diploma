package com.diploma.service.DbToolsService;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.sql.Connection;
import java.sql.DriverManager;
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
    public Object execute(Node node) throws Exception {
        try {
            String url = (String) node.getFields().get("url");
            String username = (String) node.getFields().get("username");
            String password = (String) node.getFields().get("password");
            String driver = (String) node.getFields().get("driver");

            return connect(url, username, password, driver);
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() DB Connector: " + e.getMessage());
        }
    }

    public Map<String, String> connect(String url, String username, String password, String driver) throws Exception {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, username, password);
            
            String sessionId = UUID.randomUUID().toString();
            
            connectionPoolService.addConnection(sessionId, connection);
            
            return Map.of("sessionId", sessionId);
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении DB Connector: " + e.getMessage());
        }
    }
}