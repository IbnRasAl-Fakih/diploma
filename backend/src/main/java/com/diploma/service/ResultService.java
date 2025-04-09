package com.diploma.service;

import com.diploma.dto.ResultRequestDto;
import com.diploma.dto.ResultResponseDto;
import com.diploma.model.Result;
import com.diploma.repository.ResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final ResultRepository resultRepository;

    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public ResultResponseDto create(ResultRequestDto dto) {
        Result result = new Result();
        result.setNodeId(dto.getNodeId());
        result.setWorkflowId(dto.getWorkflowId());
        result.setResult(dto.getResult());
        result.setCreatedAt(LocalDateTime.now());
        result.setUpdatedAt(LocalDateTime.now());

        Result saved = resultRepository.save(result);
        return mapToDto(saved);
    }

    public List<ResultResponseDto> findByWorkflowId(UUID workflowId) {
        return resultRepository.findByWorkflowId(workflowId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ResultResponseDto> getById(UUID nodeId) {
        return resultRepository.findById(nodeId).map(this::mapToDto);
    }

    public ResultResponseDto update(UUID nodeId, ResultRequestDto dto) {
        Result result = resultRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Result не найден"));

        result.setWorkflowId(dto.getWorkflowId());
        result.setNodeId(dto.getNodeId());
        result.setResult(dto.getResult());
        result.setUpdatedAt(LocalDateTime.now());

        return mapToDto(resultRepository.save(result));
    }

    public void delete(UUID nodeId) {
        resultRepository.deleteById(nodeId);
    }

    private ResultResponseDto mapToDto(Result r) {
        return new ResultResponseDto(
                r.getNodeId(),
                r.getWorkflowId(),
                r.getResult(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
