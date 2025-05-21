package com.diploma.service.DbToolsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;
import com.diploma.utils.FindNodeService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_reader")
public class DbReaderService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbReaderService.class);
    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;
    private final ResultService resultService;

    public DbReaderService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService, ResultService resultService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ DB Reader: Missing input node.");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            List<Map<String, Object>> data = resultService.getDataFromNode(node.getInputs().get(0).getNodeId());
            String statementQuery = data.get(0).get("sqlCommand").toString();

            if (statementQuery == null || statementQuery.isBlank()) {
                throw new NodeExecutionException("❌ DB Rader: Failed to get the result of the previous node.");
            }

            List<Map<String, Object>> result = executeQuery(sessionId.toString(), statementQuery);
            return Map.of("result", result);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Reader execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Reader execution failed.");
        }
    }

    public List<Map<String, Object>> executeQuery(String sessionId, String statementQuery) throws Exception {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            Connection connection = connectionPoolService.getConnection(sessionId);
            if (connection == null) {
                throw new NodeExecutionException("❌ DB Reader: Database connection not found.");
            }

            statement = connection.createStatement();

            boolean hasResultSet = statement.execute(statementQuery);

            if (hasResultSet) {
                resultSet = statement.getResultSet();
                int columnCount = resultSet.getMetaData().getColumnCount();

                List<Map<String, Object>> resultList = new ArrayList<>();

                while (resultSet.next()) {
                    Map<String, Object> rowMap = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = resultSet.getMetaData().getColumnName(i);
                        Object columnValue = resultSet.getObject(i);
                        rowMap.put(columnName, columnValue);
                    }
                    resultList.add(rowMap);
                }

                return resultList;
            }

            return new ArrayList<>();

        } catch (NodeExecutionException e) {
            throw e;

        } catch (SQLSyntaxErrorException e) {
            throw new NodeExecutionException("❌ DB Reader: Invalid SQL syntax – " + e.getMessage());

        } catch (SQLTimeoutException e) {
            throw new NodeExecutionException("❌ DB Reader: Query timed out.");

        } catch (SQLNonTransientConnectionException e) {
            throw new NodeExecutionException("❌ DB Reader: Connection lost – " + e.getMessage());

        } catch (SQLDataException e) {
            throw new NodeExecutionException("❌ DB Reader: Data type mismatch – " + e.getMessage());

        } catch (SQLException e) {
            throw new NodeExecutionException("❌ DB Reader: SQL error – " + e.getMessage());
        
        } catch (Exception e) {
            log.error("DB Reader execution failed", e);
            throw new NodeExecutionException("❌ DB Reader: Unknown error.");
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
    }
}