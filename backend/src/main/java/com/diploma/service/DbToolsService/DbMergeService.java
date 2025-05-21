package com.diploma.service.DbToolsService;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_merge")
public class DbMergeService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbMergeService.class);
    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;
    private final ResultService resultService;

    public DbMergeService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService, ResultService resultService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty() || node.getInputs().size() != 2) {
            throw new NodeExecutionException("❌ DB Merge: Missing input nodes.");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();
            String tableName = (String) node.getFields().get("tableName");

            if (tableName == null || tableName == "") {
                throw new NodeExecutionException("❌ DB Merge: Missing required fields.");
            }

            UUID inputNodeId = node.getInputs().get(1).getNodeId();
            List<Map<String, Object>> body = resultService.getDataFromNode(inputNodeId);

            if (body == null) {
                throw new NodeExecutionException("❌ DB Merge: Failed to get the result of the previous node.");
            }

            return merge(sessionId.toString(), tableName, body);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Merge execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Merge execution failed.");
        }
    }

    public Map<String, String> merge(String sessionId, String tableName, List<Map<String, Object>> body) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new NodeExecutionException("❌ DB Merge: Database connection not found.");
        }

        try (Statement stmt = connection.createStatement()) {
            for (Map<String, Object> row : body) {
                String whereClause = buildWhereClause(row);
                String existsQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s", tableName, whereClause);

                var rs = stmt.executeQuery(existsQuery);
                rs.next();
                boolean exists = rs.getInt(1) > 0;

                if (exists) {
                    String updateQuery = buildUpdateQuery(tableName, row, whereClause);
                    stmt.executeUpdate(updateQuery);
                } else {
                    String insertQuery = buildInsertQuery(tableName, row);
                    stmt.executeUpdate(insertQuery);
                }
            }

            return Map.of("message", "✅ Data written successfully!");

        } catch (NodeExecutionException e) {
            throw e;

        } catch (SQLSyntaxErrorException e) {
            throw new NodeExecutionException("❌ DB Merge: Invalid SQL syntax – " + e.getMessage());

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new NodeExecutionException("❌ DB Merge: Integrity constraint violation – " + e.getMessage());

        } catch (SQLDataException e) {
            throw new NodeExecutionException("❌ DB Merge: Data type mismatch – " + e.getMessage());

        } catch (SQLTimeoutException e) {
            throw new NodeExecutionException("❌ DB Merge: Timeout occurred during operation.");

        } catch (SQLNonTransientConnectionException e) {
            throw new NodeExecutionException("❌ DB Merge: Lost database connection – " + e.getMessage());

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Merge: SQL error – " + e.getMessage());

        } catch (Exception e) {
            log.error("DB Merge execution failed", e);
            throw new NodeExecutionException("❌ DB Merge: Unknown error.");
        }
    }

    private String buildWhereClause(Map<String, Object> row) {
        if (row.containsKey("id")) {
            return "id = '" + row.get("id").toString().replace("'", "''") + "'";
        } else {
            return row.entrySet().stream()
                    .map(e -> e.getKey() + " = '" + e.getValue().toString().replace("'", "''") + "'")
                    .reduce((a, b) -> a + " AND " + b)
                    .orElse("1=0");
        }
    }

    private String buildInsertQuery(String tableName, Map<String, Object> row) {
        String columns = String.join(", ", row.keySet());
        String values = row.values().stream()
                .map(v -> v == null ? "NULL" : "'" + v.toString().replace("'", "''") + "'")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, columns, values);
    }

    private String buildUpdateQuery(String tableName, Map<String, Object> row, String whereClause) {
        String setClause = row.entrySet().stream()
                .map(e -> e.getKey() + " = " + (e.getValue() == null ? "NULL" : "'" + e.getValue().toString().replace("'", "''") + "'"))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return String.format("UPDATE %s SET %s WHERE %s;", tableName, setClause, whereClause);
    }
}