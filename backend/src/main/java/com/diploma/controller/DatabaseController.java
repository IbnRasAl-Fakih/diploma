package com.diploma.controller;

import com.diploma.dto.DatabaseConnectionRequest;
import com.diploma.service.DatabaseConnectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DatabaseController {

    private final DatabaseConnectorService databaseConnectorService;

    public DatabaseController(DatabaseConnectorService databaseConnectorService) {
        this.databaseConnectorService = databaseConnectorService;
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connectToDatabase(@RequestBody DatabaseConnectionRequest request) {
        boolean isConnected = databaseConnectorService.testConnection(
                request.getDatabaseType(),
                request.getUrl(),
                request.getUsername(),
                request.getPassword(),
                request.getDriver()
        );

        if (isConnected) {
            return ResponseEntity.ok("Подключение к " + request.getDatabaseType() + " успешно!");
        } else {
            return ResponseEntity.status(500).body("Ошибка подключения к " + request.getDatabaseType());
        }
    }
}
