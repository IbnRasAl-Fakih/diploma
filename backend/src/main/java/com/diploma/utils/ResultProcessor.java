package com.diploma.utils;

import com.diploma.dto.ResultProcessorDto;
import com.diploma.model.Result;
import com.diploma.repository.ResultRepository;
import com.diploma.dto.ResultResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ResultProcessor {

    private final ResultRepository repository;

    public ResultProcessor(ResultRepository repository) {
        this.repository = repository;
    }

    public ResultResponseDto putToDatabase(ResultProcessorDto dto) {
        System.out.println("************************************"); //delete
        System.out.println(dto.getResult().toString());               //delete
        System.out.println("************************************"); //delete
        Map<String, Object> normalized = normalizeToStandardFormat(dto.getResult());
    
        try {
            Result result = Result.builder()
                    .nodeId(dto.getNodeId())
                    .workflowId(dto.getWorkflowId())
                    .result(normalized)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
    
            Result saved = repository.save(result);
    
            return ResultResponseDto.builder()
                    .nodeId(saved.getNodeId())
                    .workflowId(saved.getWorkflowId())
                    .result(normalized)
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(saved.getUpdatedAt())
                    .build();
    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении результата: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> normalizeToStandardFormat(Object rawJson) {
        if (rawJson instanceof List<?> list) {
            return Map.of("list", list);
        }
    
        if (rawJson instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getValue() instanceof List<?>) {
                    return Map.of("list", entry.getValue());
                }
            }
            return Map.of("list", List.of(rawMap));
        }
    
        throw new IllegalArgumentException("Unsupported JSON structure: expected object or array");
    }
}
