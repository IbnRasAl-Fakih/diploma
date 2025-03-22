package com.diploma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public DatabaseQueryExecuter(DatabaseQueryExecuterService databaseQueryExecuterService,
                                 DatabaseConnectionPoolService connectionPoolService,
                                 ObjectMapper objectMapper) {
        this.databaseQueryExecuterService = databaseQueryExecuterService;
        this.connectionPoolService = connectionPoolService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/execute")
    public ResponseEntity<?> executeQuery(
            @RequestParam String key,
            @RequestParam String sessionId,
            @RequestBody String sql,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {

        if (!connectionPoolService.hasConnection(sessionId)) {
            return ResponseEntity.status(404).body("❌ Сессия не найдена: " + sessionId);
        }

        try {
            ArrayNode result = databaseQueryExecuterService.execute(key, sessionId, sql, offset, limit);
            return ResponseEntity.ok(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка выполнения запроса: " + e.getMessage());
        }
    }
}
