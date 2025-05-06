package com.diploma.service.PreprocessService;

import org.springframework.stereotype.Service;

import com.diploma.dto.PreprocessDto.MissingValuesRequest;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.*;

@Service
@NodeType("missing_data_processing")
public class MissingDataProcessingService implements NodeExecutor {

    private final ResultService resultService;

    public MissingDataProcessingService(ResultService resultService) {
        this.resultService = resultService;
    }

@Override
public Object execute(Map<String, Object> fields, List<String> inputs) {
    if (inputs.isEmpty()) {
        throw new IllegalArgumentException("Missing Data Processing требует хотя бы один input (nodeId)");
    }


    UUID inputNodeId = UUID.fromString(inputs.get(0));

    List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

    MissingValuesRequest request = new MissingValuesRequest();
    request.setData(data);
    request.setActions((Map<String, String>) fields.get("actions"));
    request.setFixValues((Map<String, Object>) fields.get("fixValues"));

    List<Map<String, Object>> processedData = processMissingValues(request);

    return Map.of("processedData", processedData);
}


    public List<Map<String, Object>> processMissingValues(MissingValuesRequest request) {
        List<Map<String, Object>> data = request.getData();
        Map<String, String> actions = request.getActions();
        Map<String, Object> fixValues = request.getFixValues();

        if (data == null || data.isEmpty()) {
            return data;
        }

        Set<String> headers = data.get(0).keySet();

        for (Map<String, Object> row : data) {
            for (String column : headers) {
                Object value = row.get(column);
                if (value == null || value.toString().isEmpty()) {
                    String action = actions.getOrDefault(column, "Skip");

                    switch (action) {
                        case "Fix Value" -> row.put(column, fixValues.getOrDefault(column, "default_value"));
                        case "Most Frequent Value" -> row.put(column, getMostFrequentValue(data, column));
                        case "Next Value" -> row.put(column, getNextValue(data, data.indexOf(row), column));
                        case "Previous Value" -> row.put(column, getPreviousValue(data, data.indexOf(row), column));
                        case "Remove Row" -> row.clear();
                        case "Skip" -> {
                        }
                        default -> {
                        }
                    }
                }
            }
        }
        return data;
    }

    private String getMostFrequentValue(List<Map<String, Object>> data, String column) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (Map<String, Object> row : data) {
            Object value = row.get(column);
            if (value != null && !value.toString().isEmpty()) {
                frequencyMap.put(value.toString(), frequencyMap.getOrDefault(value.toString(), 0) + 1);
            }
        }
        return frequencyMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("0");
    }

    private String getNextValue(List<Map<String, Object>> data, int rowIndex, String column) {
        for (int i = rowIndex + 1; i < data.size(); i++) {
            Object value = data.get(i).get(column);
            if (value != null && !value.toString().isEmpty()) {
                return value.toString();
            }
        }
        return "0";
    }

    private String getPreviousValue(List<Map<String, Object>> data, int rowIndex, String column) {
        for (int i = rowIndex - 1; i >= 0; i--) {
            Object value = data.get(i).get(column);
            if (value != null && !value.toString().isEmpty()) {
                return value.toString();
            }
        }
        return "0";
    }
}
