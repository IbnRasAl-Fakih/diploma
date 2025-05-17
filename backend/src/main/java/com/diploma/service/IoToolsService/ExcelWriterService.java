package com.diploma.service.IoToolsService;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.List;
import java.util.Map;

@Service
@NodeType("excel_writer")
public class ExcelWriterService implements NodeExecutor {

    private final ResultService resultService;

    public ExcelWriterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("Excel Writer требует хотя бы один input (nodeId)");
        }

        try {
            List<Map<String, Object>> data = resultService.getDataFromNode(node.getInputs().get(0).getNodeId());

            return data;

        } catch (Exception e) {
            throw new Exception("Ошибка при выполнении execute() Excel Writer: " + e.getMessage());
        }
    }
}