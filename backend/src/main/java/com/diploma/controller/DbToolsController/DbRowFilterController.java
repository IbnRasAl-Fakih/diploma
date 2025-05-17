package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DbRowFilterRequest;
import com.diploma.service.DbToolsService.DbRowFilterService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DbRowFilterController {

    private final DbRowFilterService dbRowFilterService;

    public DbRowFilterController(DbRowFilterService dbRowFilterService) {
        this.dbRowFilterService = dbRowFilterService;
    }

    @PostMapping("/row-filter")
    public ResponseEntity<?> applyFilter(@RequestBody DbRowFilterRequest request) {
        try {
            Map<String, Object> response = dbRowFilterService.filter(request.getSessionId(), request.getTableName(), request.getFilters());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}