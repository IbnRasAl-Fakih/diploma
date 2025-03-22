package com.diploma.controller;

import com.diploma.dto.DatabaseConnectionRequest;
import com.diploma.service.DatabaseConnectionPoolService;
import com.diploma.service.DatabaseConnectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/db")
public class DatabaseController {

    private final DatabaseConnectorService connectorService;
    private final DatabaseConnectionPoolService connectionPoolService;

    public DatabaseController(DatabaseConnectorService connectorService,
                              DatabaseConnectionPoolService connectionPoolService) {
        this.connectorService = connectorService;
        this.connectionPoolService = connectionPoolService;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectToDatabase(@RequestBody DatabaseConnectionRequest request) {
        try {
            String sessionId = connectorService.connect(
                    request.getUrl(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getDriver()
            );
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка подключения: " + e.getMessage());
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect(@RequestParam String sessionId) {
        if (connectionPoolService.hasConnection(sessionId)) {
            connectionPoolService.removeConnection(sessionId);
            return ResponseEntity.ok("✅ Соединение успешно закрыто для сессии: " + sessionId);
        } else {
            return ResponseEntity.status(404).body("❌ Сессия не найдена: " + sessionId);
        }
    }
}
