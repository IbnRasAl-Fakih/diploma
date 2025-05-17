package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DatabaseReaderRequest;
import com.diploma.service.DbToolsService.DatabaseReaderService;
import com.diploma.utils.DatabaseConnectionPoolService;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DatabaseReaderController {

    private final DatabaseReaderService databaseQueryExecuterService;
    private final DatabaseConnectionPoolService connectionPoolService;

    public DatabaseReaderController(DatabaseReaderService databaseQueryExecuterService,DatabaseConnectionPoolService connectionPoolService) {
        this.databaseQueryExecuterService = databaseQueryExecuterService;
        this.connectionPoolService = connectionPoolService;
    }

    @PostMapping("/read")
    public ResponseEntity<?> executeQuery(@RequestBody DatabaseReaderRequest request) {

        if (!connectionPoolService.hasConnection(request.getSessionId())) {
            return ResponseEntity.status(404).body("❌ Сессия не найдена: " + request.getSessionId());
        }

        try {
            List<Map<String, Object>> result = databaseQueryExecuterService.executeQuery(request.getSessionId(), request.getStatement());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка выполнения запроса: " + e.getMessage());
        }
    }
}