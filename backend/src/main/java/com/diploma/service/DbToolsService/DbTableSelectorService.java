package com.diploma.service.DbToolsService;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_table_selector")
public class DbTableSelectorService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbTableSelectorService.class);
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
            throw new NodeExecutionException("❌ DB Table Selector: Missing input node.");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector").node();
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();
            String tableName = (String) node.getFields().get("tableName");

            if (tableName == null || tableName == "") {
                throw new NodeExecutionException("❌ DB Table Selector: Missing required fields.");
            }

            List<Map<String, String>> result = getTableColumns(sessionId.toString(), tableName);
            return Map.of("result", result);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Table Selector execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Table Selector execution failed.");
        }
    }

    public List<Map<String, String>> getTableColumns(String sessionId, String tableName) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new NodeExecutionException("❌ DB Table Selector: Database connection not found.");
        }

        DatabaseMetaData metaData = connection.getMetaData();

        boolean tableExists = false;
        try (ResultSet tables = metaData.getTables(null, null, tableName, new String[] { "TABLE" })) {
            while (tables.next()) {
                String foundTable = tables.getString("TABLE_NAME");
                if (foundTable != null && foundTable.equalsIgnoreCase(tableName)) {
                    tableExists = true;
                    break;
                }
            }
        }

        if (!tableExists) {
            throw new NodeExecutionException("❌ DB Table Selector: Table '" + tableName + "' does not exist.");
        }

        List<Map<String, String>> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                columns.add(Map.of("columnName", columnName, "dataType", dataType));
            }

        return columns;

        } catch (NodeExecutionException e) {
            throw e;

        } catch (SQLTimeoutException e) {
            throw new NodeExecutionException("❌ DB Table Selector: Timeout during column metadata retrieval.");

        } catch (SQLNonTransientConnectionException e) {
            throw new NodeExecutionException("❌ DB Table Selector: Lost connection – " + e.getMessage());

        } catch (SQLSyntaxErrorException e) {
            throw new NodeExecutionException("❌ DB Table Selector: Invalid table name – " + e.getMessage());

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Table Selector: SQL error – " + e.getMessage());

        } catch (Exception e) {
            log.error("DB Table Selector execution failed", e);
            throw new NodeExecutionException("❌ DB Table Selector: Unknown error.");
        }
    }
}