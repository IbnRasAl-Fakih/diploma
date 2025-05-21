package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.NodeJoinRequest;
import com.diploma.exception.NodeExecutionException;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.model.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NodeType("node_join")
public class NodeJoinService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(NodeJoinService.class);
    private final ResultService resultService;

    public NodeJoinService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Joiner: Missing input nodes.");
        }

        try {
            UUID inputNodeIdA = node.getInputs().get(0).getNodeId();
            UUID inputNodeIdB = node.getInputs().get(1).getNodeId();

            List<Map<String, Object>> tableA = resultService.getDataFromNode(inputNodeIdA);
            List<Map<String, Object>> tableB = resultService.getDataFromNode(inputNodeIdB);

            if (tableA == null || tableB == null) {
                throw new NodeExecutionException("❌ Joiner: Failed to get the result of the previous node.");
            }

            NodeJoinRequest request = new NodeJoinRequest();
            request.setTableA(tableA);
            request.setTableB(tableB);
            request.setJoinColumn((String) node.getFields().get("joinColumn"));

            List<Map<String, Object>> result = joinTables(request);

            return Map.of("result", result);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Joiner execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Joiner execution failed.");
        }
    }
    
    public List<Map<String, Object>> joinTables(NodeJoinRequest request) throws Exception {
        try {

            if (request.getJoinColumn() == null) {
                throw new NodeExecutionException("❌ Joiner: Required fields are null.");
            }

            List<Map<String, Object>> tableA = request.getTableA();
            List<Map<String, Object>> tableB = request.getTableB();
            String joinColumn = request.getJoinColumn();

            if (tableA.isEmpty() || tableB.isEmpty()) {
                throw new NodeExecutionException("❌ Joiner: Both tables must contain data.");
            }

            Map<Object, Map<String, Object>> tableBMap = new HashMap<>();
            for (Map<String, Object> row : tableB) {
                Object joinValue = row.get(joinColumn);

                if (joinValue == null) continue;

                if (tableBMap.containsKey(joinValue)) {
                    throw new NodeExecutionException("❌ Joiner: Duplicate join key in table B: " + joinValue);
                }

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

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Joiner: Invalid key type or data format.");

        } catch (Exception e) {
            log.error("Joiner execution failed", e);
            throw new NodeExecutionException("❌ Joiner: Unknown error.");
        }
    }
}