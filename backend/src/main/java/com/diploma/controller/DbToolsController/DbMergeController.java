package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DbMergeRequest;
import com.diploma.service.DbToolsService.DbMergeService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DbMergeController {

    private final DbMergeService dbMergeService;

    public DbMergeController(DbMergeService dbMergeService) {
        this.dbMergeService = dbMergeService;
    }

    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody DbMergeRequest request) {
        try {
            Map<String, String> result = dbMergeService.merge(request.getSessionId(), request.getTableName(), request.getBody());
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}