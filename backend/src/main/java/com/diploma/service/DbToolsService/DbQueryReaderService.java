package com.diploma.service.DbToolsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;
import com.diploma.utils.FindNodeService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("db_query_reader")
public class DbQueryReaderService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbQueryReaderService.class);
    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;

    public DbQueryReaderService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ DB Query Reader: Missing input node.");
        }

        try {
            Node dataContainsNode = findNodeService.findNode(node, "db_connector");
            UUID sessionId = sessionService.getByNodeId(dataContainsNode.getNodeId()).getSessionId();

            String statementQuery = (String) node.getFields().get("statementQuery");

            if (statementQuery == null || statementQuery == "") {
                throw new NodeExecutionException("❌ DB Query Reader: Missing required fields.");
            }

            List<Map<String, Object>> result = executeQuery(sessionId.toString(), statementQuery);
            return Map.of("result", result);

        } catch (Exception e) {
            log.error("DB Query Reader execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Query Reader execution failed: ", e);
        }
    }

    public List<Map<String, Object>> executeQuery(String sessionId, String statementQuery) throws Exception {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            Connection connection = connectionPoolService.getConnection(sessionId);
            if (connection == null) {
                throw new NodeExecutionException("❌ DB Query Reader: Database connection not found");
            }

            statement = connection.createStatement();

            boolean hasResultSet = statement.execute(statementQuery);

            if (hasResultSet) {
                resultSet = statement.getResultSet();
                int columnCount = resultSet.getMetaData().getColumnCount();

                List<Map<String, Object>> resultList = new ArrayList<>();

                while (resultSet.next()) {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
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
        } catch (Exception e) {
            log.error("DB Query Reader execution failed", e);
            throw new NodeExecutionException("❌ DB Query Reader: ", e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
    }
}