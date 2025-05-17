package com.diploma.service.DbToolsService;

import com.diploma.dto.DbToolsDto.FilterCondition;
import com.diploma.model.Node;
import com.diploma.utils.ColumnTypeService;
import com.diploma.utils.DatabaseConnectionPoolService;
import com.diploma.utils.FindNodeService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.SessionService;

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
            throw new IllegalArgumentException("DB Row Filter требует хотя бы один input (nodeId)");
        }

        try {
            UUID sessionId = sessionService.getByNodeId(findNodeService.findNode(node, "db_connector").getNodeId()).getSessionId();
            String tableName = (String) findNodeService.findNode(node, "db_table_selector").getFields().get("tableName");
            Object rawFilters = node.getFields().get("filters");

            List<FilterCondition> filters = objectMapper.convertValue(rawFilters, new TypeReference<List<FilterCondition>>() {});

            return filter(sessionId.toString(), tableName, filters);

        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() DB Row Filter: " + e.getMessage());
        }
    }

    public Map<String, Object> filter(String sessionId, String tableName, List<FilterCondition> filters) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);
        if (connection == null) {
            throw new IllegalArgumentException("Invalid sessionId or connection not found");
        }

        StringBuilder whereClause = new StringBuilder();
        StringJoiner whereConditions = new StringJoiner(" AND ");

        for (FilterCondition condition : filters) {
            whereConditions.add(condition.getColumn() + " " + condition.getOperator() + " ?");
        }

        if (!filters.isEmpty()) {
            whereClause.append(" WHERE ").append(whereConditions);
        }

        String userSql = "SELECT * FROM " + tableName + whereClause;
        String countSql = "SELECT COUNT(*) FROM " + tableName + whereClause;

        int count;
        try (PreparedStatement stmt = connection.prepareStatement(countSql)) {
            for (int i = 0; i < filters.size(); i++) {
                FilterCondition filter = filters.get(i);
                String dataType = columnTypeService.getColumnType(sessionId, tableName, filter.getColumn());

                System.out.println("DataType: " + dataType); // delete

                int paramIndex = i + 1;
                String val = filter.getValue();

                switch (dataType.toLowerCase()) {
                    case "bool":
                    case "boolean":
                        stmt.setBoolean(paramIndex, Boolean.parseBoolean(val));
                        break;
                    case "int4": // PostgreSQL integer
                    case "int8": // bigint
                    case "int2": // smallint
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
        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении DB Row Filter: " + e.getMessage());
        }

        return Map.of("sqlCommand", userSql, "count", count);
    }
}