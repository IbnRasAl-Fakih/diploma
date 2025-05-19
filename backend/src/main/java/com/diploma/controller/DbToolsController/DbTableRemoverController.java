package com.diploma.controller.DbToolsController;

import com.diploma.dto.DbToolsDto.DbTableRemoveRequest;
import com.diploma.service.DbToolsService.DbTableRemoverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
public class DbTableRemoverController {

    private final DbTableRemoverService dbTableRemoverService;

    public DbTableRemoverController(DbTableRemoverService dbTableRemoverService) {
        this.dbTableRemoverService = dbTableRemoverService;
    }

    @PostMapping("/remove-table")
    public ResponseEntity<String> removeTable(@RequestBody DbTableRemoveRequest request) {
        try {
            dbTableRemoverService.removeTable(request.getSessionId(), request.getTableName());
            return ResponseEntity.ok("Table '" + request.getTableName() + "' removed successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error removing table: " + e.getMessage());
        }
    }
}