package com.diploma.service;

import com.diploma.dto.ResultRequestDto;
import com.diploma.dto.ResultResponseDto;
import com.diploma.model.Result;
import com.diploma.repository.ResultRepository;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
        result.setType(dto.getType());
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

    public Optional<ResultResponseDto> getById(UUID nodeId, int offset, int limit) {
        return resultRepository.findById(nodeId).map(result -> {
            Map<String, Object> fullResult = result.getResult();
    
            // Получаем список из ключа "list"
            List<?> originalList = (List<?>) fullResult.getOrDefault("list", List.of());
    
            // Применяем offset и limit
            int fromIndex = Math.min(offset, originalList.size());
            int toIndex = Math.min(offset + limit, originalList.size());
            List<?> paginatedList = originalList.subList(fromIndex, toIndex);
    
            // Обновляем list в результате
            fullResult.put("list", paginatedList);
    
            return ResultResponseDto.builder()
                    .nodeId(result.getNodeId())
                    .type(result.getType())
                    .workflowId(result.getWorkflowId())
                    .result(fullResult)
                    .createdAt(result.getCreatedAt())
                    .updatedAt(result.getUpdatedAt())
                    .build();
        });
    }

    public ResultResponseDto update(UUID nodeId, ResultRequestDto dto) {
        Result result = resultRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Result не найден"));

        result.setWorkflowId(dto.getWorkflowId());
        result.setType(dto.getType());
        result.setNodeId(dto.getNodeId());
        result.setResult(dto.getResult());
        result.setUpdatedAt(LocalDateTime.now());

        return mapToDto(resultRepository.save(result));
    }

    public void delete(UUID nodeId) {
        resultRepository.deleteById(nodeId);
    }

    public void deleteByWorkflowId(UUID workflowId) {
        resultRepository.deleteByWorkflowId(workflowId);
    }

    public List<Map<String, Object>> getDataFromNode(UUID nodeId) {
        return getById(nodeId, 0, Integer.MAX_VALUE)
                .map(dto -> {
                    Object list = dto.getResult().get("list");
                    if (list instanceof List<?> rawList) {
                        if (!rawList.isEmpty() && rawList.get(0) instanceof Map<?, ?>) {
                            return (List<Map<String, Object>>) list;
                        }
                    }
                    throw new IllegalStateException("Результат node " + nodeId + " не содержит допустимых данных");
                })
                .orElseThrow(() -> new NoSuchElementException("Результат с nodeId " + nodeId + " не найден"));
    }

    private ResultResponseDto mapToDto(Result r) {
        return new ResultResponseDto(
                r.getNodeId(),
                r.getType(),
                r.getWorkflowId(),
                r.getResult(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}