package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DbTableSelectorRequest;
import com.diploma.service.DbToolsService.DbTableSelectorService;
import com.diploma.utils.DatabaseConnectionPoolService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db")
public class DbTableSelectorController {

    private final DbTableSelectorService dbTableSelectorService;
    private final DatabaseConnectionPoolService connectionPoolService;

    public DbTableSelectorController(DbTableSelectorService dbTableSelectorService, DatabaseConnectionPoolService connectionPoolService) {
        this.dbTableSelectorService = dbTableSelectorService;
        this.connectionPoolService = connectionPoolService;
    }

    @PostMapping("/table-selector")
    public ResponseEntity<?> getTableColumns(@RequestBody DbTableSelectorRequest request) {

        if (!connectionPoolService.hasConnection(request.getSessionId())) {
            return ResponseEntity.status(404).body("❌ Сессия не найдена: " + request.getSessionId());
        }

        try {
            List<Map<String, String>> columns = dbTableSelectorService.getTableColumns(request.getSessionId(), request.getTableName());
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}