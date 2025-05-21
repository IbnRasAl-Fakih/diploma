package com.diploma.service.DbToolsService;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_connector")
public class DatabaseConnectorService implements NodeExecutor{

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectorService.class);
    private final DatabaseConnectionPoolService connectionPoolService;

    public DatabaseConnectorService(DatabaseConnectionPoolService connectionPoolService) {
        this.connectionPoolService = connectionPoolService;
    }

    @Override
    public Object execute(Node node) {
        try {
            String url = (String) node.getFields().get("url");
            String username = (String) node.getFields().get("username");
            String password = (String) node.getFields().get("password");
            String driver = (String) node.getFields().get("driver");

            if (url == null || username == null || password == null || driver == null || url == "" || username == "" || password == "" || driver == "") {
                throw new NodeExecutionException("❌ DB Connector: Missing required fields.");
            }

            return connect(url, username, password, driver);
        } catch (NodeExecutionException e) {
            throw e;
        } catch (Exception e) {
            log.error("DB Connector execution failed", e);
            throw new NodeExecutionException("❌ DB Connector: ", e);
        }
    }

    public Map<String, String> connect(String url, String username, String password, String driver) {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, username, password);

            String sessionId = UUID.randomUUID().toString();
            connectionPoolService.addConnection(sessionId, connection);

            return Map.of("sessionId", sessionId);

        } catch (ClassNotFoundException e) {
            throw new NodeExecutionException("❌ DB Connector: JDBC Driver not found - " + driver, e);

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Connector: Failed to connect to database - " + e.getMessage(), e);

        } catch (Exception e) {
            throw new NodeExecutionException("❌ Unexpected error in DB Connector", e);
        }
    }
}