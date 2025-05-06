package com.diploma.service.PreprocessService;


import org.springframework.stereotype.Service;

import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.*;
import java.util.stream.Collectors;

@Service
@NodeType("column_filter")
public class ColumnFilterService implements NodeExecutor {

    private final ResultService resultService;

    public ColumnFilterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Map<String, Object> fields, List<String> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("ColumnFilter требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = UUID.fromString(inputs.get(0));

        List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

        @SuppressWarnings("unchecked")
        List<String> columnsToRemove = (List<String>) fields.getOrDefault("columnsToRemove", List.of());

        List<Map<String, Object>> filtered = removeColumns(data, new HashSet<>(columnsToRemove));

        return Map.of("list", filtered); 
    }


    public List<Map<String, Object>> removeColumns(List<Map<String, Object>> rows, Set<String> columnsToRemove) {
        return rows.stream()
                .map(row -> {
                    Map<String, Object> filtered = new LinkedHashMap<>(row);
                    columnsToRemove.forEach(filtered::remove);
                    return filtered;
                })
                .collect(Collectors.toList());
    }
}
