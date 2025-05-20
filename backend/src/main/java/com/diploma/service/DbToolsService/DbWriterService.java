package com.diploma.service.DbToolsService;

import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@NodeType("db_writer")
public class DbWriterService implements NodeExecutor {

    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;
    private final ResultService resultService;

    public DbWriterService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService, ResultService resultService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("DB Writer требует хотя бы один input (nodeId)");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();
            String tableName = (String) node.getFields().get("tableName");

            UUID inputNodeId = node.getInputs().get(1).getNodeId();
            List<Map<String, Object>> body = resultService.getDataFromNode(inputNodeId);

            return write(sessionId.toString(), tableName, body);
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() DB Writer" + e.getMessage());
        }
    }

    public Map<String, String> write(String sessionId, String tableName, List<Map<String, Object>> body) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new IllegalArgumentException("Invalid sessionId or connection not found");
        }

        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Request body is empty");
        }

        try (Statement stmt = connection.createStatement()) {
            for (Map<String, Object> row : body) {
                String columns = String.join(", ", row.keySet());
                String values = row.values().stream()
                        .map(val -> {
                            if (val == null) return "NULL";
                            String stringVal = val.toString().replace("'", "''");
                            return "'" + stringVal + "'";
                        })
                        .collect(Collectors.joining(", "));

                String safeTable = tableName.replaceAll("[^a-zA-Z0-9_]", "");
                String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", safeTable, columns, values);
                stmt.executeUpdate(sql);
            }

            return Map.of("message", "✅ Data written successfully: " + body.size() + " rows.");
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении DB Writer: " + e.getMessage(), e);
        }
    }
}