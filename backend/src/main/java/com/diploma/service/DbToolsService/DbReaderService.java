package com.diploma.service.DbToolsService;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;
import com.diploma.utils.FindNodeService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_reader")
public class DbReaderService implements NodeExecutor {

    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;
    private final ResultService resultService;

    public DbReaderService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService, ResultService resultService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("DB Reader требует хотя бы один input (nodeId)");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            List<Map<String, Object>> data = resultService.getDataFromNode(node.getInputs().get(0).getNodeId());
            String statementQuery = data.get(0).get("sqlCommand").toString();

            if (statementQuery == null || statementQuery.isBlank()) {
                throw new IllegalArgumentException("Поле 'statementQuery' не должно быть пустым");
            }

            List<Map<String, Object>> result = executeQuery(sessionId.toString(), statementQuery);
            return Map.of("result", result);

        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() DB Reader: " + e.getMessage());
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
            System.out.println(e);
            throw new Exception("Ошибка при выполнении DB Reader: " + e.getMessage());
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
    }
}