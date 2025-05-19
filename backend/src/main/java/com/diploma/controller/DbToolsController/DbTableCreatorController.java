package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DbTableCreatorRequest;
import com.diploma.service.DbToolsService.DbTableCreatorService;
import com.diploma.utils.DatabaseConnectionPoolService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DbTableCreatorController {

    private final DbTableCreatorService tableCreatorService;
    private final DatabaseConnectionPoolService connectionPoolService;

    public DbTableCreatorController(DbTableCreatorService tableCreatorService, DatabaseConnectionPoolService connectionPoolService) {
        this.tableCreatorService = tableCreatorService;
        this.connectionPoolService = connectionPoolService;
    }

    @PostMapping("/table-create")
    public ResponseEntity<?> createTable(@RequestBody DbTableCreatorRequest request) {
        if (!connectionPoolService.hasConnection(request.getSessionId())) {
            return ResponseEntity.status(404).body("❌ Сессия не найдена: " + request.getSessionId());
        }

        try {
            tableCreatorService.createTable(request.getSessionId(), request.getTableName(), request.getColumns());
            return ResponseEntity.ok("Succsess");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка выполнения запроса: " + e.getMessage());
        }
    }
}