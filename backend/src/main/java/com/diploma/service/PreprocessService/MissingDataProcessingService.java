package com.diploma.service.PreprocessService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.*;

@Service
@NodeType("missing_data_processing")
public class MissingDataProcessingService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(MissingDataProcessingService.class);
    private final ResultService resultService;

    public MissingDataProcessingService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Missing Data Processing: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();

            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Missing Data Processing: Failed to get the result of the previous node.");
            }

            List<Map<String, Object>> processedData = processMissingValues(data, (Map<String, String>) node.getFields().get("actions"), (Map<String, Object>) node.getFields().get("fixValues"));

            return Map.of("processedData", processedData);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Missing Data Processing execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Missing Data Processing execution failed.");
        }
    }

    public List<Map<String, Object>> processMissingValues(List<Map<String, Object>> data, Map<String, String> actions, Map<String, Object> fixValues) throws Exception {
        try {
            if (actions == null) {
                throw new NodeExecutionException("❌ Missing Data Processing: Action map is missing.");
            }

            if (fixValues == null) {
                throw new NodeExecutionException("❌ Missing Data Processing: Fix values map is missing.");
            }

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

        } catch (NodeExecutionException e) {
            throw e;

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Missing Data Processing: Invalid input types.");

        } catch (Exception e) {
            log.error("Missing Data Processing execution failed", e);
            throw new NodeExecutionException("❌ Missing Data Processing: Unknown error.");
        }
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