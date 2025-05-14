package com.diploma.service.PreprocessService;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.*;
import java.util.stream.Collectors;

@Service
@NodeType("duplicate_remover")
public class DuplicateRemoverService implements NodeExecutor {

    private final ResultService resultService;

    public DuplicateRemoverService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("Duplicate Remover требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = node.getInputs().get(0).getNodeId();

        List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

        @SuppressWarnings("unchecked")
        List<String> selectedColumns = (List<String>) node.getFields().getOrDefault("selectedColumns", List.of());

        List<Map<String, Object>> cleanData = removeDuplicates(data, selectedColumns);

        return Map.of("cleanData", cleanData);
    }

    public List<Map<String, Object>> removeDuplicates(List<Map<String, Object>> data, List<String> selectedColumns) {
        if (data == null || data.isEmpty()) {
            return List.of(); 
        }

        Set<List<Object>> seen = new LinkedHashSet<>();
        List<Map<String, Object>> cleanData = new ArrayList<>();

        for (Map<String, Object> row : data) {
            List<Object> key = selectedColumns.stream()
                    .map(row::get)
                    .collect(Collectors.toList());

            if (!seen.add(key)) {
                continue;
            }

            cleanData.add(row);
        }

        return cleanData;
    }
}