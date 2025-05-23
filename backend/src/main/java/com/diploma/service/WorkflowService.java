package com.diploma.service;

import com.diploma.dto.WorkflowRequestDto;
import com.diploma.dto.WorkflowResponseDto;
import com.diploma.model.Workflow;
import com.diploma.repository.WorkflowRepository;
import com.diploma.utils.DatabaseCleanerService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    private final DatabaseCleanerService dbCleanerService;
    private final WorkflowRepository repository;

    public WorkflowService(WorkflowRepository repository, DatabaseCleanerService dbCleanerService) {
        this.repository = repository;
        this.dbCleanerService = dbCleanerService;
    }

    public WorkflowResponseDto create(WorkflowRequestDto dto) {
        Workflow workflow = new Workflow();
        workflow.setId(UUID.randomUUID());
        workflow.setTitle(dto.getTitle());
        workflow.setOwnerId(dto.getOwnerId());
        workflow.setStructure(dto.getStructure());
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());

        Workflow saved = repository.save(workflow);
        return toDto(saved);
    }

    public List<WorkflowResponseDto> findByOwner(UUID ownerId) {
        return repository.findByOwnerId(ownerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<WorkflowResponseDto> findById(UUID id) {
        return repository.findById(id).map(this::toDto);
    }

    public WorkflowResponseDto update(UUID id, WorkflowRequestDto dto) {
        Workflow existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Workflow не найден"));

        existing.setStructure(dto.getStructure());
        existing.setOwnerId(dto.getOwnerId());
        existing.setTitle(dto.getTitle());
        existing.setUpdatedAt(LocalDateTime.now());

        return toDto(repository.save(existing));
    }

    public void delete(UUID id) throws Exception{
        dbCleanerService.clean(id);
        repository.deleteById(id);
    }

    private WorkflowResponseDto toDto(Workflow wf) {
        return new WorkflowResponseDto(
                wf.getId(),
                wf.getTitle(),
                wf.getOwnerId(),
                wf.getStructure(),
                wf.getCreatedAt(),
                wf.getUpdatedAt()
        );
    }
}