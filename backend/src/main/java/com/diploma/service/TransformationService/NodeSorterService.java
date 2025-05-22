package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.NodeSorterRequest;
import com.diploma.exception.NodeExecutionException;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.model.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@NodeType("node_sorter")
public class NodeSorterService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(NodeSorterService.class);
    private final ResultService resultService;

    public NodeSorterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Sorter: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();

            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Sorter: Failed to get the result of the previous node.");
            }

            Object rawColumns = node.getFields().get("columns");
            if (!(rawColumns instanceof List<?> rawList)) {
                throw new NodeExecutionException("❌ Sorter: 'columns' field must be a list.");
            }

            List<String> columns;
            try {
                columns = rawList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                throw new NodeExecutionException("❌ Sorter: Failed to parse 'columns' list.");
            }

            if (columns.isEmpty()) {
                throw new NodeExecutionException("❌ Sorter: 'columns' list is empty.");
            }

            List<String> missingColumns = columns.stream()
                .filter(col -> data.stream().noneMatch(row -> row.containsKey(col)))
                .collect(Collectors.toList());

            if (!missingColumns.isEmpty()) {
                throw new NodeExecutionException("❌ Sorter: The following columns are missing in data: " + missingColumns);
            }

            Boolean isAscending = (boolean) node.getFields().getOrDefault("ascending", true);
            
            List<Map<String, Object>> sortedData = sortData(data, columns, isAscending);

            return Map.of("result", sortedData);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Sorter execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Sorter execution failed.");
        }
    }

    public List<Map<String, Object>> sortData(List<Map<String, Object>> data, List<String> columns, boolean isAscending) throws Exception {
        try {
            if (data == null || data.isEmpty()) return data;
            if (columns == null || columns.isEmpty()) {
                throw new NodeExecutionException("❌ Sorter: No columns provided for sorting.");
            }

            Comparator<Map<String, Object>> comparator = (row1, row2) -> {
                for (String column : columns) {
                    Object val1 = row1.get(column);
                    Object val2 = row2.get(column);

                    // Обработка null'ов — null считается "меньше"
                    if (val1 == null && val2 == null) continue;
                    if (val1 == null) return -1;
                    if (val2 == null) return 1;

                    if (!(val1 instanceof Comparable) || !(val2 instanceof Comparable)) {
                        throw new NodeExecutionException("❌ Sorter: Non-comparable values in column '" + column + "'");
                    }

                    @SuppressWarnings("unchecked")
                    int cmp = ((Comparable<Object>) val1).compareTo(val2);
                    if (cmp != 0) return cmp;
                }
                return 0;
            };

            List<Map<String, Object>> sorted = new ArrayList<>(data);

            sorted.sort(isAscending ? comparator : comparator.reversed());

            return sorted;

        } catch (NodeExecutionException e) {
            throw e;

        } catch (ClassCastException e) {
            log.error("❌ Sorter: Invalid type during sorting", e);
            throw new NodeExecutionException("❌ Sorter: Sorting failed due to incompatible types.", e);

        } catch (Exception e) {
            log.error("❌ Sorter: Unexpected error", e);
            throw new NodeExecutionException("❌ Sorter: Unknown error during sorting.", e);
        }
    }
}