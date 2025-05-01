package com.diploma.controller;

import com.diploma.service.WorkflowExecutorService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.diploma.dto.WorkflowExecutorRequestDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow-executor")
public class WorkflowExecutorController {

    private final WorkflowExecutorService executorService;

    public WorkflowExecutorController(WorkflowExecutorService executorService) {
        this.executorService = executorService;
    }

    @PostMapping("/execute")
    @Tag(name = "ExecutorController")
    public ResponseEntity<String> executeWorkflow(@RequestBody WorkflowExecutorRequestDto dto) {
        try {
            executorService.executeWorkflow(dto);
            return ResponseEntity.ok("✅ Workflow executed");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Execution failed: " + e.getMessage());
        }
    }
}