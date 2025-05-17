package com.diploma.utils;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Service
public class ColumnTypeService {

    private final DatabaseConnectionPoolService connectionPoolService;

    public ColumnTypeService(DatabaseConnectionPoolService connectionPoolService) {
        this.connectionPoolService = connectionPoolService;
    }

    public String getColumnType(String sessionId, String tableName, String columnName) throws Exception {
        Connection connection = connectionPoolService.getConnection(sessionId);

        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return rs.getString("TYPE_NAME");
            } else {
                throw new IllegalArgumentException("Error in ColumnTypeService - Column not found: " + columnName);
            }
        }
    }
}