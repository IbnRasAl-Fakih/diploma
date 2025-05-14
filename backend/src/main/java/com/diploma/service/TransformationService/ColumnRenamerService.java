package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnRenamerRequest;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NodeType("column_renamer")
public class ColumnRenamerService implements NodeExecutor {

    private final ResultService resultService;

    public ColumnRenamerService(ResultService resultService) {
        this.resultService = resultService;
    }
    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("ColumnRenamerService требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = node.getInputs().get(0).getNodeId();
        List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

        ColumnRenamerRequest request = new ColumnRenamerRequest();
        request.setData(data);
        request.setRenameMap((Map<String, String>) node.getFields().get(" "));

        List<Map<String, Object>> result = renameColumns(request);

        return Map.of("result", result);
    }

    public List<Map<String, Object>> renameColumns(ColumnRenamerRequest req) {
        List<Map<String, Object>> renamed = new ArrayList<>();

        for (Map<String, Object> row : req.getData()) {
            Map<String, Object> newRow = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String originalKey = entry.getKey();
                String renamedKey = req.getRenameMap().getOrDefault(originalKey, originalKey);
                newRow.put(renamedKey, entry.getValue());
            }

            renamed.add(newRow);
        }

        return renamed;
    }
}