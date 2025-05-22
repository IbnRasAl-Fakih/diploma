package com.diploma.service.DbToolsService;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;
import com.diploma.utils.FindNodeService.FoundNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@NodeType("db_writer")
public class DbWriterService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbWriterService.class);
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
        if (node.getInputs().isEmpty() || node.getInputs().size() != 2) {
            throw new NodeExecutionException("❌ DB Writer: Missing input nodes.");
        }

        try {
            FoundNode dataContainsNode = findNodeService.findNode(node, "db_connector");

            int index = (dataContainsNode.inputIndex() == 0) ? 1 : 0;

            UUID sessionId = sessionService.getByNodeId(dataContainsNode.node().getNodeId()).getSessionId();
            String tableName = (String) node.getFields().get("tableName");

            if (tableName == null || tableName == "") {
                throw new NodeExecutionException("❌ DB Writer: Missing required fields.");
            }

            UUID inputNodeId = node.getInputs().get(index).getNodeId();
            List<Map<String, Object>> body = resultService.getDataFromNode(inputNodeId);

            if (body == null) {
                throw new NodeExecutionException("❌ DB Writer: Failed to get the result of the previous node.");
            }

            return write(sessionId.toString(), tableName, body);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Writer execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Writer execution failed.");
        }
    }

    public Map<String, String> write(String sessionId, String tableName, List<Map<String, Object>> body) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new NodeExecutionException("❌ DB Writer: Database connection not found.");
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
            
        } catch (NodeExecutionException e) {
            throw e;

        } catch (SQLSyntaxErrorException e) {
            throw new NodeExecutionException("❌ DB Writer: Invalid SQL syntax – " + e.getMessage());

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new NodeExecutionException("❌ DB Writer: Integrity constraint violation – " + e.getMessage());

        } catch (SQLTimeoutException e) {
            throw new NodeExecutionException("❌ DB Writer: Timeout during insert operation.");

        } catch (SQLDataException e) {
            throw new NodeExecutionException("❌ DB Writer: Data type mismatch – " + e.getMessage());

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Writer: SQL error – " + e.getMessage());

        } catch (Exception e) {
            log.error("DB Writer execution failed", e);
            throw new NodeExecutionException("❌ DB Writer: ", e);
        }
    }
}