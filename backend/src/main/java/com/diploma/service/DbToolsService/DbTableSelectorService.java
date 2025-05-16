package com.diploma.service.DbToolsService;

import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindDbConnectorNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

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
    private final FindDbConnectorNodeService findDbConnectorNodeService;

    public DbTableSelectorService(DatabaseConnectionPoolService connectionPoolService, FindDbConnectorNodeService findDbConnectorNodeService) {
        this.connectionPoolService = connectionPoolService;
        this.findDbConnectorNodeService = findDbConnectorNodeService;
    }

    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("DB Table Selector требует хотя бы один input (nodeId)");
        }

        try {
            UUID sessionId = findDbConnectorNodeService.findDbConnectorNodeId(node);
            String tableName = (String) node.getFields().get("tableName");
            String schemeName = (String) node.getFields().get("schemeName");

            List<Map<String, String>> result = getTableColumns(sessionId.toString(), tableName);
            return Map.of("result", result);

        } catch (Exception e) {
            return Map.of("message", "Error" + e.getMessage());
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
        }

        return columns;
    }
}