package com.diploma.service.DbToolsService;

import com.diploma.dto.DbToolsDto.FilterCondition;
import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.utils.ColumnTypeService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@NodeType("db_row_filter")
public class DbRowFilterService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DbRowFilterService.class);
    private final DatabaseConnectionPoolService connectionPoolService;
    private final FindNodeService findNodeService;
    private final SessionService sessionService;
    private final ColumnTypeService columnTypeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DbRowFilterService(DatabaseConnectionPoolService connectionPoolService, FindNodeService findNodeService, SessionService sessionService, ColumnTypeService columnTypeService) {
        this.connectionPoolService = connectionPoolService;
        this.findNodeService = findNodeService;
        this.sessionService = sessionService;
        this.columnTypeService = columnTypeService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ DB Row Filter: Missing input node.");
        }

        try {
            UUID sessionId = sessionService.getByNodeId(findNodeService.findNode(node, "db_connector").getNodeId()).getSessionId();
            String tableName = (String) node.getFields().get("tableName");

            Object rawFilters = node.getFields().get("filters");

            if (tableName == null || tableName == "" || rawFilters == null || rawFilters == "") {
                throw new NodeExecutionException("❌ DB Row Filter: Missing required fields.");
            }

            List<FilterCondition> filters;

            if (rawFilters instanceof List<?>) {
                filters = objectMapper.convertValue(rawFilters, new TypeReference<List<FilterCondition>>() {});
            } else if (rawFilters instanceof Map) {
                FilterCondition single = objectMapper.convertValue(rawFilters, FilterCondition.class);
                filters = List.of(single);
            } else {
                throw new NodeExecutionException("❌ DB Row Filter: 'filters' must be an object or a list of objects.");
            }

            return filter(sessionId.toString(), tableName, filters);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Row Filter execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DB Row Filter execution failed.");
        }
    }

    public Map<String, Object> filter(String sessionId, String tableName, List<FilterCondition> filters) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new NodeExecutionException("❌ DB Row Filter: Database connection not found");
        }

        // Используем точное имя таблицы, не изменяя регистр
        String tableNameToUse = "\"" + tableName + "\""; // Экранируем название таблицы в кавычки

        StringJoiner whereConditionsForExec = new StringJoiner(" AND ");
        StringJoiner whereConditionsForUserSql = new StringJoiner(" AND ");

        for (FilterCondition filter : filters) {
            String column = filter.getColumn();
            String operator = filter.getOperator();
            String val = filter.getValue();

            String dataType = columnTypeService.getColumnType(sessionId, tableName, column).toLowerCase();

            String userCondition;
            if ("bool".equals(dataType) || "boolean".equals(dataType)) {
                userCondition = column + " " + operator + " " + Boolean.parseBoolean(val);
            } else if ("int2".equals(dataType) || "int4".equals(dataType) || "int8".equals(dataType) ||
                    "float4".equals(dataType) || "float8".equals(dataType) ||
                    "numeric".equals(dataType) || "decimal".equals(dataType)) {
                userCondition = column + " " + operator + " " + val;
            } else {
                String escapedVal = val.replace("'", "''");
                userCondition = column + " " + operator + " '" + escapedVal + "'";
            }
            whereConditionsForUserSql.add(userCondition);

            whereConditionsForExec.add(column + " " + operator + " ?");
        }

        String whereClauseForExec = filters.isEmpty() ? "" : " WHERE " + whereConditionsForExec.toString();
        String whereClauseForUserSql = filters.isEmpty() ? "" : " WHERE " + whereConditionsForUserSql.toString();

        String userSql = "SELECT * FROM " + tableNameToUse + whereClauseForUserSql;
        String countSql = "SELECT COUNT(*) FROM " + tableNameToUse + whereClauseForExec;

        int count;
        try (PreparedStatement stmt = connection.prepareStatement(countSql)) {
            for (int i = 0; i < filters.size(); i++) {
                FilterCondition filter = filters.get(i);
                String dataType = columnTypeService.getColumnType(sessionId, tableName, filter.getColumn()).toLowerCase();

                int paramIndex = i + 1;
                String val = filter.getValue();

                switch (dataType) {
                    case "bool":
                    case "boolean":
                        stmt.setBoolean(paramIndex, Boolean.parseBoolean(val));
                        break;
                    case "int4":
                    case "int8":
                    case "int2":
                        stmt.setInt(paramIndex, Integer.parseInt(val));
                        break;
                    case "float4":
                    case "float8":
                    case "numeric":
                    case "decimal":
                        stmt.setDouble(paramIndex, Double.parseDouble(val));
                        break;
                    default:
                        stmt.setString(paramIndex, val);
                        break;
                }
            }

            ResultSet rs = stmt.executeQuery();
            count = rs.next() ? rs.getInt(1) : 0;

            return Map.of("sqlCommand", userSql, "count", count);
        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("DB Row Filter execution failed", e);
            throw new NodeExecutionException("❌ DB Row Filter: Unknown error.");
        }
    }
}