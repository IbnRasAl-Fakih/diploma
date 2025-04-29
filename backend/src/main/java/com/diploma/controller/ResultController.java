package com.diploma.controller;

import com.diploma.dto.ResultRequestDto;
import com.diploma.dto.ResultResponseDto;
import com.diploma.service.ResultService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService service;

    public ResultController(ResultService service) {
        this.service = service;
    }

    @PostMapping
    @Tag(name = "Results")
    public ResponseEntity<?> create(@RequestBody ResultRequestDto dto) {
        try {
            ResultResponseDto created = service.create(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при создании: " + e.getMessage()));
        }
    }

    @GetMapping("/{workflowId}")
    @Tag(name = "Results")
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
    public ResponseEntity<?> getById(
            @PathVariable UUID nodeId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            Optional<ResultResponseDto> result = service.getById(nodeId, offset, limit);
            return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "Result не найден")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при получении: " + e.getMessage()));
        }
    }

    @PutMapping("/{nodeId}")
    @Tag(name = "Results")
    public ResponseEntity<?> update(@PathVariable UUID nodeId, @RequestBody ResultRequestDto dto) {
        try {
            ResultResponseDto updated = service.update(nodeId, dto);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при обновлении: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{nodeId}")
    @Tag(name = "Results")
    public ResponseEntity<?> delete(@PathVariable UUID nodeId) {
        try {
            service.delete(nodeId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при удалении: " + e.getMessage()));
        }
    }
}
