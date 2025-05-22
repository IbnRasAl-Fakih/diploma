package com.diploma.utils;

import com.diploma.dto.ResultProcessorDto;
import com.diploma.model.Result;
import com.diploma.repository.ResultRepository;
import com.diploma.dto.ResultResponseDto;
import com.diploma.exception.NodeExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ResultProcessor {

    private static final Logger log = LoggerFactory.getLogger(ResultProcessor.class);
    private final ResultRepository repository;

    public ResultProcessor(ResultRepository repository) {
        this.repository = repository;
    }

    public ResultResponseDto putToDatabase(ResultProcessorDto dto) {
        try {
            if (dto.getResult() == null) {
                throw new NodeExecutionException("❌ Result data is null.");
            }

            Map<String, Object> normalized = normalizeToStandardFormat(dto.getResult());

            Result result = Result.builder()
                    .nodeId(dto.getNodeId())
                    .type(dto.getType())
                    .workflowId(dto.getWorkflowId())
                    .result(normalized)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Result saved = repository.save(result);

            return ResultResponseDto.builder()
                    .nodeId(saved.getNodeId())
                    .type(saved.getType())
                    .workflowId(saved.getWorkflowId())
                    .result(normalized)
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(saved.getUpdatedAt())
                    .build();

        } catch (NodeExecutionException e) {
            throw e;

        } catch (IllegalArgumentException e) {
            throw new NodeExecutionException("❌ Server error - Result Processor: Invalid input format: " + e.getMessage(), e);

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Server error - Result Processor: Type casting error while normalizing result structure: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Failed to save result", e);
            throw new NodeExecutionException("❌ Server error - Result Processor: Error while saving result: " + e.getMessage());
        }
    }

    public Map<String, Object> normalizeToStandardFormat(Object rawJson) throws Exception {
        try {
            if (rawJson instanceof List<?> list) {
                List<Object> fixedList = fixListElements(list);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("list", fixedList);
                return result;
            }
        
            if (rawJson instanceof Map<?, ?> rawMap) {
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getValue() instanceof List<?> innerList) {
                        List<Object> fixedList = fixListElements(innerList);
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("list", fixedList);
                        return result;
                    }
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("list", List.of(fixMap(rawMap)));
                return result;
            }

            return null;
            
        } catch (IllegalArgumentException e) {
            throw new NodeExecutionException("❌ Unsupported input format: " + e.getMessage(), e);

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Invalid data type in JSON structure.", e);

        } catch (NullPointerException e) {
            throw new NodeExecutionException("❌ Malformed JSON structure (null key or value).", e);
        }
    }
    
    private List<Object> fixListElements(List<?> list) {
        List<Object> fixed = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> mapItem) {
                fixed.add(fixMap(mapItem));
            } else {
                fixed.add(item);
            }
        }
        return fixed;
    }
    
    private Map<String, Object> fixMap(Map<?, ?> rawMap) {
        LinkedHashMap<String, Object> fixedMap = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            fixedMap.put(entry.getKey().toString(), entry.getValue());
        }
        return fixedMap;
    } 
}