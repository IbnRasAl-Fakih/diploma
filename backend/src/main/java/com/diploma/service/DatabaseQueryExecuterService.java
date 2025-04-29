package com.diploma.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Service
public class DatabaseQueryExecuterService {

    private final DatabaseConnectionPoolService connectionPoolService;
    private final GlobalDataStorageService globalDataStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DatabaseQueryExecuterService(DatabaseConnectionPoolService connectionPoolService, GlobalDataStorageService globalDataStorageService) {
        this.connectionPoolService = connectionPoolService;
        this.globalDataStorageService = globalDataStorageService;
    }

    public ArrayNode execute(String key, String sessionId, String statementQuery, int offset, int limit) throws Exception {
        if (offset > 0 && globalDataStorageService.hasResult(key)) {
            return globalDataStorageService.getResults(offset, limit, key);
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = connectionPoolService.getConnection(sessionId);
            statement = connection.createStatement();

            boolean hasResultSet = statement.execute(statementQuery);

            if (hasResultSet) {
                resultSet = statement.getResultSet();
                int columnCount = resultSet.getMetaData().getColumnCount();
                ArrayNode jsonArray = objectMapper.createArrayNode();

                while (resultSet.next()) {
                    ObjectNode rowObject = objectMapper.createObjectNode();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = resultSet.getMetaData().getColumnName(i);
                        String columnValue = resultSet.getString(i);
                        rowObject.put(columnName, columnValue);
                    }
                    jsonArray.add(rowObject);
                }

                globalDataStorageService.saveResult(key, jsonArray);
                return globalDataStorageService.getResults(offset, limit, key);
            }

        } catch (Exception e) {
            throw new Exception("Error executing statement: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }

        return objectMapper.createArrayNode();
    }
}
