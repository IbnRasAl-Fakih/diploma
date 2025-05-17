package com.diploma.service.DbToolsService;

import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_table_selector")
public class DbTableSelectorService implements NodeExecutor {

    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;

    public DbTableSelectorService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("DB Table Selector требует хотя бы один input (nodeId)");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();
            String tableName = (String) node.getFields().get("tableName");

            List<Map<String, String>> result = getTableColumns(sessionId.toString(), tableName);
            return Map.of("result", result);

        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() DB Table Selector" + e.getMessage());
        }
    }

    public List<Map<String, String>> getTableColumns(String sessionId, String tableName) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new IllegalArgumentException("Invalid sessionId or connection not found");
        }

        DatabaseMetaData metaData = connection.getMetaData();

        List<Map<String, String>> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                columns.add(Map.of("columnName", columnName, "dataType", dataType));
            }
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении DB Table Selector: " + e.getMessage());
        }

        return columns;
    }
}