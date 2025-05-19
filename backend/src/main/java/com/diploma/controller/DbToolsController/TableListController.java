package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.TableListRequest;
import com.diploma.service.DbToolsService.TableListService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/db")
public class TableListController {

    private final TableListService tableListService;

    public TableListController(TableListService tableListService) {
        this.tableListService = tableListService;
    }

    @PostMapping("/tables")
    public ResponseEntity<?> listTables(@RequestBody TableListRequest request) {
        try {
            Map<String, Object> tables = tableListService.listTables(request.getSessionId());
            return ResponseEntity.ok(tables);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Ошибка получения таблиц: " + e.getMessage());
        }
    }
}