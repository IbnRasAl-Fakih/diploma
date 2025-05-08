package com.diploma.service.TransformationService;

import com.diploma.dto.PreprocessDto.RowFilterRequest;
import com.diploma.dto.TransformationDto.ColumnAppenderRequest;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NodeType("column_appender")
public class ColumnAppenderService implements NodeExecutor {

    private final ResultService resultService;

    public ColumnAppenderService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Map<String, Object> fields, List<String> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Duplicate Remover требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = UUID.fromString(inputs.get(0));
        UUID inputNodeId2 = UUID.fromString(inputs.get(1));

        List<Map<String, Object>> data1 = resultService.getDataFromNode(inputNodeId);
        List<Map<String, Object>> data2 = resultService.getDataFromNode(inputNodeId2);

        ColumnAppenderRequest request = new ColumnAppenderRequest();
        request.setTableA(data1);
        request.setTableB(data2);
    
        List<Map<String, Object>> result = appendColumns(request);

        return Map.of("result", result);
    }

    public List<Map<String, Object>> appendColumns(ColumnAppenderRequest req) {
        List<Map<String, Object>> a = req.getTableA();
        List<Map<String, Object>> b = req.getTableB();

        if (a.size() != b.size()) {
            throw new IllegalArgumentException("Tables must have the same number of rows.");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < a.size(); i++) {
            Map<String, Object> rowA = a.get(i);
            Map<String, Object> rowB = b.get(i);

            Map<String, Object> merged = new LinkedHashMap<>();
            merged.putAll(rowA);
            merged.putAll(rowB); 

            result.add(merged);
        }

        return result;
    }
}
