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
import java.util.stream.Collectors;

@Service
@NodeType("duplicate_remover")
public class DuplicateRemoverService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DuplicateRemoverService.class);
    private final ResultService resultService;

    public DuplicateRemoverService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Duplicate Remover: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();

            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Duplicate Remover: Failed to get the result of the previous node.");
            }

            @SuppressWarnings("unchecked")
            List<String> selectedColumns = (List<String>) node.getFields().getOrDefault("selectedColumns", List.of());

            List<Map<String, Object>> cleanData = removeDuplicates(data, selectedColumns);

            return Map.of("cleanData", cleanData);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Duplicate Remover execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Duplicate Remover execution failed.");
        }
    }

    public List<Map<String, Object>> removeDuplicates(List<Map<String, Object>> data, List<String> selectedColumns) throws Exception {
        if (selectedColumns == null) {
            throw new NodeExecutionException("❌ Duplicate Remover: No columns specified for comparison.");
        }

        try {
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

        } catch (NodeExecutionException e) {
            throw e;

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Duplicate Remover: Invalid row format – " + e.getMessage());

        } catch (Exception e) {
            log.error("Duplicate Remover execution failed", e);
            throw new NodeExecutionException("❌ Duplicate Remover: Unknown error.");
        }
    }
}