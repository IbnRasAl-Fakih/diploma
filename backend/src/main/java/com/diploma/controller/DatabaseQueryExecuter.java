package com.diploma.controller;

import com.diploma.service.DatabaseConnectionPoolService;
import com.diploma.service.DatabaseQueryExecuterService;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DatabaseQueryExecuter {

    private final DatabaseQueryExecuterService databaseQueryExecuterService;
    private final DatabaseConnectionPoolService connectionPoolService;

    public DatabaseQueryExecuter(DatabaseQueryExecuterService databaseQueryExecuterService,
                                 DatabaseConnectionPoolService connectionPoolService) {
        this.databaseQueryExecuterService = databaseQueryExecuterService;
        this.connectionPoolService = connectionPoolService;
    }

    @PostMapping("/execute")
    public ResponseEntity<?> executeQuery(
            @RequestParam String key,
            @RequestParam String sessionId,
            @RequestBody String sql,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {

        try {
            ArrayNode result = databaseQueryExecuterService.execute(key, sessionId, sql, offset, limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка выполнения запроса: " + e.getMessage());
        }
}
}
