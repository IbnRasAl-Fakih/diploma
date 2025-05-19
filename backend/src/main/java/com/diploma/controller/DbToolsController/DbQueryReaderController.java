package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DbQueryReaderRequest;
import com.diploma.service.DbToolsService.DbQueryReaderService;
import com.diploma.utils.DatabaseConnectionPoolService;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DbQueryReaderController {

    private final DbQueryReaderService dbQueryReaderService;
    private final DatabaseConnectionPoolService connectionPoolService;

    public DbQueryReaderController(DbQueryReaderService dbQueryReaderService,DatabaseConnectionPoolService connectionPoolService) {
        this.dbQueryReaderService = dbQueryReaderService;
        this.connectionPoolService = connectionPoolService;
    }

    @PostMapping("/read")
    public ResponseEntity<?> executeQuery(@RequestBody DbQueryReaderRequest request) {

        if (!connectionPoolService.hasConnection(request.getSessionId())) {
            return ResponseEntity.status(404).body("❌ Сессия не найдена: " + request.getSessionId());
        }

        try {
            List<Map<String, Object>> result = dbQueryReaderService.executeQuery(request.getSessionId(), request.getStatement());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка выполнения запроса: " + e.getMessage());
        }
    }
}