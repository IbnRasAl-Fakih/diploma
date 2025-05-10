package com.diploma.controller.DbToolsController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.diploma.service.DbToolsService.DatabaseQueryExecuterService;
import com.diploma.utils.DatabaseConnectionPoolService;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DatabaseQueryExecuter {

    private final DatabaseQueryExecuterService databaseQueryExecuterService;
    private final DatabaseConnectionPoolService connectionPoolService;

    public DatabaseQueryExecuter(DatabaseQueryExecuterService databaseQueryExecuterService,DatabaseConnectionPoolService connectionPoolService) {
        this.databaseQueryExecuterService = databaseQueryExecuterService;
        this.connectionPoolService = connectionPoolService;
    }

    @PostMapping("/execute")
    public ResponseEntity<?> executeQuery(
            @RequestParam String sessionId,
            @RequestBody String sql) {

        if (!connectionPoolService.hasConnection(sessionId)) {
            return ResponseEntity.status(404).body("❌ Сессия не найдена: " + sessionId);
        }

        try {
            List<Map<String, Object>> result = databaseQueryExecuterService.executeQuery(sessionId, sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка выполнения запроса: " + e.getMessage());
        }
    }
}
