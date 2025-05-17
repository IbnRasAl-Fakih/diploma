package com.diploma.service.IoToolsService;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.List;
import java.util.Map;

@Service
@NodeType("csv_writer")
public class CsvWriter implements NodeExecutor {

    private final ResultService resultService;

    public CsvWriter(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("CSV Writer требует хотя бы один input (nodeId)");
        }

        try {
            List<Map<String, Object>> data = resultService.getDataFromNode(node.getInputs().get(0).getNodeId());

            return data;

        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() CSV Writer: " + e.getMessage());
        }
    }
}