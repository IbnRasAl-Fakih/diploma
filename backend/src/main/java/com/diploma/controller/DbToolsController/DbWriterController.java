package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DbWriterRequest;
import com.diploma.service.DbToolsService.DbWriterService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DbWriterController {

    private final DbWriterService dbWriterService;

    public DbWriterController(DbWriterService dbWriterService) {
        this.dbWriterService = dbWriterService;
    }

    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody DbWriterRequest request) {
        try {
            Map<String, String> result = dbWriterService.write(request.getSessionId(), request.getTableName(), request.getBody());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
}