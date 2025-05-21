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
@NodeType("column_filter")
public class ColumnFilterService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(ColumnFilterService.class);
    private final ResultService resultService;

    public ColumnFilterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Column Filter: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();

            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Column Filter: Failed to get the result of the previous node.");
            }

            @SuppressWarnings("unchecked")
            List<String> columnsToRemove = (List<String>) node.getFields().getOrDefault("columnsToRemove", List.of());

            List<Map<String, Object>> filtered = removeColumns(data, new HashSet<>(columnsToRemove));

            return Map.of("list", filtered);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Column Filter execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Column Filter execution failed.");
        }
    }


    public List<Map<String, Object>> removeColumns(List<Map<String, Object>> rows, Set<String> columnsToRemove) throws Exception {
        if (columnsToRemove == null) {
            throw new NodeExecutionException("❌ Column Filter: Columns to remove not provided.");
        }

        try {
            return rows.stream()
                .map(row -> {
                    Map<String, Object> filtered = new LinkedHashMap<>(row);
                    columnsToRemove.forEach(filtered::remove);
                    return filtered;
                })
                .collect(Collectors.toList());

        } catch (NodeExecutionException e) {
            throw e;

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Column Filter: Row structure is invalid.");

        } catch (Exception e) {
            log.error("Column Filter execution failed", e);
            throw new NodeExecutionException("❌ Column Filter: Unknown error.");
        }
    }
}