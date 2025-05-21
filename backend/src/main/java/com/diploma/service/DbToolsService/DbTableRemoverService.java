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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_table_remover")
public class DbTableRemoverService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbTableRemoverService.class);
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
            throw new NodeExecutionException("❌ DB Table Remover: Missing input node.");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            String tableName = (String) node.getFields().get("tableName");
            
            if (tableName == null || tableName == "") {
                throw new NodeExecutionException("❌ DB Table Remover: Missing required fields.");
            }

            return removeTable(sessionId.toString(), tableName);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Table Remover execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Table Remover execution failed.");
        }
    }

    public Map<String, String> removeTable(String sessionId, String tableName) throws Exception {
        try {
            Connection connection = connectionPoolService.getConnection(sessionId);
            if (connection == null) {
                throw new NodeExecutionException("❌ DB Table Remover: Database connection not found.");
            }

            String sql = "DROP TABLE IF EXISTS " + tableName;

            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);

            return Map.of("message", "Table deleted successfully!");

        } catch (NodeExecutionException e) {
            throw e;

        } catch (SQLSyntaxErrorException e) {
            throw new NodeExecutionException("❌ DB Table Remover: Invalid SQL syntax – " + e.getMessage());

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new NodeExecutionException("❌ DB Table Remover: Constraint violation – " + e.getMessage());

        } catch (SQLTimeoutException e) {
            throw new NodeExecutionException("❌ DB Table Remover: Timeout occurred.");

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Table Remover: SQL Error – " + e.getMessage());

        } catch (Exception e) {
            log.error("DB Table Remover execution failed", e);
            throw new NodeExecutionException("❌ DB Table Remover: Unknown error.");
        }
    }
}