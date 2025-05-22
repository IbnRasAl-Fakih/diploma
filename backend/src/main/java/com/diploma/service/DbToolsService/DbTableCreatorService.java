package com.diploma.service.DbToolsService;

import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@NodeType("db_table_creator")
public class DbTableCreatorService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbTableCreatorService.class);
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
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ DB Table Creator: Missing input node.");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector").node();
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            String tableName = (String) node.getFields().get("tableName");
            Object rawColumns = node.getFields().get("columns");

            if (tableName == null || tableName == "" || rawColumns == null || rawColumns == "") {
                throw new NodeExecutionException("❌ DB Table Creator: Missing required fields.");
            }

            List<Map<String, String>> columns = objectMapper.convertValue(rawColumns, new TypeReference<List<Map<String, String>>>() {});
            
            return createTable(sessionId.toString(), tableName, columns);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Table Creator execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Table Creator execution failed.");
        }
    }

    public Map<String, String> createTable(String sessionId, String tableName, List<Map<String, String>> columns) throws Exception {
        try {
            Connection connection = connectionPoolService.getConnection(sessionId);
            if (connection == null) {
                throw new NodeExecutionException("❌ DB Table Creator: Database connection not found.");
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

        } catch (NodeExecutionException e) {
            throw e;

        } catch (SQLSyntaxErrorException e) {
            throw new NodeExecutionException("❌ DB Table Creator: Invalid SQL syntax – " + e.getMessage());

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new NodeExecutionException("❌ DB Table Creator: Integrity constraint violation – " + e.getMessage());

        } catch (SQLTimeoutException e) {
            throw new NodeExecutionException("❌ DB Table Creator: Timeout during table creation.");

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Table Creator: SQL error – " + e.getMessage());

        } catch (Exception e) {
            log.error("DB Table Creator execution failed", e);
            throw new NodeExecutionException("❌ DB Table Creator: Unknown error.");
        }
    }
}