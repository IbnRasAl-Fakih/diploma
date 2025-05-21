package com.diploma.service.DbToolsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_table_list")
public class TableListService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(TableListService.class);
    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;

    public TableListService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
    }

    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ DB Table List: Missing input node.");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            Map<String, Object> result = listTables(sessionId.toString());
            return result;
            
        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Db Table List execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Table List execution failed.");
        }
    }

    public Map<String, Object> listTables(String sessionId) {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new NodeExecutionException("❌ DB Table List: Database connection not found.");
        }

        List<Map<String, String>> tables = new ArrayList<>();

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(Map.of("tableName", rs.getString("TABLE_NAME")));
                }
            }

            return Map.of("tables", tables);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (SQLTimeoutException e) {
            throw new NodeExecutionException("❌ DB Table List: Metadata query timed out.");

        } catch (SQLNonTransientConnectionException e) {
            throw new NodeExecutionException("❌ DB Table List: Connection lost – " + e.getMessage());

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Table List: SQL Error – " + e.getMessage());

        } catch (Exception e) {
            log.error("DB Table List execution failed", e);
            throw new NodeExecutionException("❌ DB Table List: Unknown error.");
        }
    }
}