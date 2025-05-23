package com.diploma.controller;

import com.diploma.dto.ResultResponseDto;
import com.diploma.service.ResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService service;

    public ResultController(ResultService service) {
        this.service = service;
    }

    @GetMapping("/{workflowId}")
    @Tag(name = "Results")
    @Operation(summary = "Получение всех результатов по ID Процесса")
    public ResponseEntity<?> getByWorkflow(@PathVariable UUID workflowId) {
        try {
            List<ResultResponseDto> results = service.findByWorkflowId(workflowId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при получении: " + e.getMessage()));
        }
    }

    @GetMapping("/single/{nodeId}")
    @Tag(name = "Results")
    @Operation(summary = "Получение резльтата по ID")
    public ResponseEntity<?> getById(@PathVariable UUID nodeId, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            Optional<ResultResponseDto> result = service.getById(nodeId, offset, limit);
            return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "Result не найден")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при получении: " + e.getMessage()));
        }
    }
}
