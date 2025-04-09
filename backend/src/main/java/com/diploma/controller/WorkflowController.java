package com.diploma.controller;

import com.diploma.dto.WorkflowRequestDto;
import com.diploma.dto.WorkflowResponseDto;
import com.diploma.service.WorkflowService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @PostMapping
    @Tag(name = "Workflows", description = "Обновление процесса || Удаление процесса || Создание процесса || Получение всех процессов по ID пользователя || Получение процесса по ID")
    public ResponseEntity<?> create(@RequestBody WorkflowRequestDto dto) {
        try {
            WorkflowResponseDto created = service.create(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при создании: " + e.getMessage()));
        }
    }

    @GetMapping("/{ownerId}")
    @Tag(name = "Workflows")
    public ResponseEntity<?> getByOwner(@PathVariable UUID ownerId) {
        try {
            List<WorkflowResponseDto> workflows = service.findByOwner(ownerId);
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при получении: " + e.getMessage()));
        }
    }

    @GetMapping("/single/{id}")
    @Tag(name = "Workflows")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            Optional<WorkflowResponseDto> result = service.findById(id);
            return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "Workflow не найден")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при получении: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Tag(name = "Workflows")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody WorkflowRequestDto dto) {
        try {
            WorkflowResponseDto updated = service.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при обновлении: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Tag(name = "Workflows")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при удалении: " + e.getMessage()));
        }
    }
}
