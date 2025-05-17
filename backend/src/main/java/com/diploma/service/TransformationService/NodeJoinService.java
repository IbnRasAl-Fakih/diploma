package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.NodeJoinRequest;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.model.Node;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NodeType("node_join")
public class NodeJoinService implements NodeExecutor {

        private final ResultService resultService;

        public NodeJoinService(ResultService resultService) {
            this.resultService = resultService;
        }
    
        @Override
        public Object execute(Node node) {
            try {
                if (node.getInputs().isEmpty()) {
                    throw new IllegalArgumentException("ColumnRenamerService требует хотя бы один input (nodeId)");
                }

                UUID inputNodeIdA = node.getInputs().get(0).getNodeId();
                UUID inputNodeIdB = node.getInputs().get(1).getNodeId();

                List<Map<String, Object>> tableA = resultService.getDataFromNode(inputNodeIdA);
                List<Map<String, Object>> tableB = resultService.getDataFromNode(inputNodeIdB);

                NodeJoinRequest request = new NodeJoinRequest();
                request.setTableA(tableA);
                request.setTableB(tableB);
                request.setJoinColumn((String) node.getFields().get("joinColumn"));

                List<Map<String, Object>> result = joinTables(request);

                return Map.of("result", result);

            } catch (IllegalArgumentException e) {
                return Map.of("status", "error", "message",
                        "Недостаточно данных для выполнения запроса: " + e.getMessage());
            } catch (Exception e) {
                return Map.of("status", "error", "message", "Ошибка выполнения запроса: " + e.getMessage());
            }
        }
    
    
    public List<Map<String, Object>> joinTables(NodeJoinRequest request) {
        List<Map<String, Object>> tableA = request.getTableA();
        List<Map<String, Object>> tableB = request.getTableB();
        String joinColumn = request.getJoinColumn();

        if (tableA == null || tableA.isEmpty() || tableB == null || tableB.isEmpty()) {
            throw new IllegalArgumentException("Обе таблицы должны содержать данные.");
        }

        Map<Object, Map<String, Object>> tableBMap = new HashMap<>();
        for (Map<String, Object> row : tableB) {
            Object joinValue = row.get(joinColumn);
            tableBMap.put(joinValue, row);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> rowA : tableA) {
            Object joinValue = rowA.get(joinColumn);
            Map<String, Object> rowB = tableBMap.get(joinValue);

            if (rowB != null) {
                Map<String, Object> joinedRow = new LinkedHashMap<>(rowA); 
                joinedRow.putAll(rowB); 
                result.add(joinedRow);
            }
        }

        return result;
    }
}
