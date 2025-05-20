package com.diploma.service.DbToolsService;

import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_table_remover")
public class DbTableRemoverService implements NodeExecutor {

    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;

    public DbTableRemoverService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("DB Table Remover требует хотя бы один input (nodeId)");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            String tableName = (String) node.getFields().get("tableName");
            
            return removeTable(sessionId.toString(), tableName);
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() DB Table Remover: " + e.getMessage());
        }
    }

    public Map<String, String> removeTable(String sessionId, String tableName) throws Exception {
        try {
            Connection connection = connectionPoolService.getConnection(sessionId);
            if (connection == null) {
                throw new IllegalArgumentException("Session not found: " + sessionId);
            }

            String sql = "DROP TABLE IF EXISTS " + tableName;

            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);

            return Map.of("message", "Table deleted successfully!");
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении DB Table Remover: " + e.getMessage(), e);
        }
    }
}