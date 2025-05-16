package com.diploma.service.DbToolsService;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindDbConnectorNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_table_list")
public class TableListService implements NodeExecutor{

    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindDbConnectorNodeService findDbConnectorNodeService;

    public TableListService(DatabaseConnectionPoolService connectionPoolService, FindDbConnectorNodeService findDbConnectorNodeService) {
        this.connectionPoolService = connectionPoolService;
        this.findDbConnectorNodeService = findDbConnectorNodeService;
    }

    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("DB TableList требует хотя бы один input (nodeId)");
        }

        try {
            UUID sessionId = findDbConnectorNodeService.findDbConnectorNodeId(node);

            List<String> result = listTables(sessionId.toString());
            return Map.of("result", result);

        } catch (Exception e) {
            return Map.of("message", "Error" + e.getMessage());
        }
    }

    public List<String> listTables(String sessionId) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }
}