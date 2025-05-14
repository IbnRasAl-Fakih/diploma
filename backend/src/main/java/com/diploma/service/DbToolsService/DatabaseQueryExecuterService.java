package com.diploma.service.DbToolsService;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_query_executor")
public class DatabaseQueryExecuterService implements NodeExecutor {

    private final DatabaseConnectionPoolService connectionPoolService;
    private final ResultService resultService;

    public DatabaseQueryExecuterService(DatabaseConnectionPoolService connectionPoolService, ResultService resultService) {
        this.connectionPoolService = connectionPoolService;
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("DB Query Executor требует хотя бы один input (nodeId)");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data.isEmpty() || !data.get(0).containsKey("sessionId")) {
                throw new IllegalStateException("Данные по nodeId отсутствуют или sessionId не найден");
            }

            String sessionId = (String) data.get(0).get("sessionId");
            String statementQuery = (String) node.getFields().get("statementQuery");

            if (statementQuery == null || statementQuery.isBlank()) {
                throw new IllegalArgumentException("Поле 'statementQuery' не должно быть пустым");
            }

            List<Map<String, Object>> result = executeQuery(sessionId, statementQuery);
            return Map.of("result", result);

        } catch (Exception e) {
            return Map.of("message", "Ошибка при выполнении DB запроса: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> executeQuery(String sessionId, String statementQuery) throws Exception {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            Connection connection = connectionPoolService.getConnection(sessionId);
            statement = connection.createStatement();

            boolean hasResultSet = statement.execute(statementQuery);

            if (hasResultSet) {
                resultSet = statement.getResultSet();
                int columnCount = resultSet.getMetaData().getColumnCount();

                List<Map<String, Object>> resultList = new ArrayList<>();

                while (resultSet.next()) {
                    Map<String, Object> rowMap = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = resultSet.getMetaData().getColumnName(i);
                        Object columnValue = resultSet.getObject(i);
                        rowMap.put(columnName, columnValue);
                    }
                    resultList.add(rowMap);
                }

                return resultList;
            }

            return new ArrayList<>();
        } catch (Exception e) {
            throw new Exception("Error executing statement: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
    }
}