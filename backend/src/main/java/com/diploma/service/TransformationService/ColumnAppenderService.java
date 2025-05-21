package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnAppenderRequest;
import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NodeType("column_appender")
public class ColumnAppenderService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(ColumnAppenderService.class);
    private final ResultService resultService;

    public ColumnAppenderService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Column Appender: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            UUID inputNodeId2 = node.getInputs().get(1).getNodeId();

            List<Map<String, Object>> data1 = resultService.getDataFromNode(inputNodeId);
            List<Map<String, Object>> data2 = resultService.getDataFromNode(inputNodeId2);

            if (data1 == null || data2 == null) {
                throw new NodeExecutionException("❌ Column Appender: Failed to get the result of the previous node.");
            }

            ColumnAppenderRequest request = new ColumnAppenderRequest();
            request.setTableA(data1);
            request.setTableB(data2);
        
            List<Map<String, Object>> result = appendColumns(request);

            return Map.of("result", result);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Column Appender execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Column Appender execution failed.");
        }
    }

    public List<Map<String, Object>> appendColumns(ColumnAppenderRequest req) throws Exception {
        try {
            List<Map<String, Object>> a = req.getTableA();
            List<Map<String, Object>> b = req.getTableB();

            if (a.size() != b.size()) {
                throw new NodeExecutionException("❌ Column Appender: Tables must have the same number of rows.");
            }

            List<Map<String, Object>> result = new ArrayList<>();

            for (int i = 0; i < a.size(); i++) {
                Map<String, Object> rowA = a.get(i);
                Map<String, Object> rowB = b.get(i);

                Set<String> duplicates = new HashSet<>(rowA.keySet());
                duplicates.retainAll(rowB.keySet());
                if (!duplicates.isEmpty()) {
                    throw new NodeExecutionException("❌ Column Appender: Duplicate columns found at index " + i + ": " + duplicates);
                }

                Map<String, Object> merged = new LinkedHashMap<>();
                merged.putAll(rowA);
                merged.putAll(rowB); 

                result.add(merged);
            }

            return result;

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Column Appender: Invalid row structure.");

        } catch (Exception e) {
            log.error("Column Appender execution failed", e);
            throw new NodeExecutionException("❌ Column Appender: Unknown error.");
        }
    }
}