package com.diploma.service.DbToolsService;

import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.diploma.model.Node;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@NodeType("db_table_creator")
public class DbTableCreatorService implements NodeExecutor {

    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DbTableCreatorService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            String tableName = (String) node.getFields().get("tableName");
            Object rawColumns = node.getFields().get("columns");

            List<Map<String, String>> columns = objectMapper.convertValue(rawColumns, new TypeReference<List<Map<String, String>>>() {});
            
            return createTable(sessionId.toString(), tableName, columns);
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() DB Table Creator: " + e.getMessage());
        }
    }

    public Map<String, String> createTable(String sessionId, String tableName, List<Map<String, String>> columns) throws Exception {
        try {
            Connection connection = connectionPoolService.getConnection(sessionId);
            if (connection == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }

            String normalizedTableName = tableName.trim().toLowerCase().replaceAll("\\s+", "_");

            String columnsSql = columns.stream()
                    .map(c -> c.get("columnName") + " " + c.get("columnType"))
                    .collect(Collectors.joining(", "));

            String createTableSql = String.format("CREATE TABLE %s (%s);", normalizedTableName, columnsSql);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(createTableSql);
            }

            return Map.of("tableName", normalizedTableName);
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении DB Table Creator: " + e.getMessage(), e);
        }
    }
}