package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.GroupByAdvancedRequest;
import com.diploma.exception.NodeExecutionException;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.model.Node;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@NodeType("Group_By")
public class GroupByAdvancedService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(GroupByAdvancedService.class);
    private final ResultService resultService;

    public GroupByAdvancedService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Group By: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Group By: Failed to get the result of the previous node.");
            }

            GroupByAdvancedRequest request = new GroupByAdvancedRequest();
            request.setData(data);
            request.setGroupByColumns((List<String>) node.getFields().get("groupByColumns"));
            request.setAggregationMapping((Map<String, String>) node.getFields().get("aggregationMapping"));

            List<Map<String, Object>> result = groupBy(request);

            return Map.of("result", result);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Group By execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Group By execution failed.");
        }
    }

    public List<Map<String, Object>> groupBy(GroupByAdvancedRequest req) throws Exception {
        try {

            if (req.getGroupByColumns() == null || req.getAggregationMapping() == null) {
                throw new NodeExecutionException("❌ Group By: Required fields are null.");
            }

            Map<String, List<Map<String, Object>>> grouped = req.getData().stream()
                .collect(Collectors.groupingBy(row -> createGroupKey(row, req.getGroupByColumns())));

            List<Map<String, Object>> result = new ArrayList<>();

            for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
                List<Map<String, Object>> groupRows = entry.getValue();
                Map<String, Object> newRow = new LinkedHashMap<>();
                Map<String, Object> exampleRow = groupRows.get(0);


                for (String groupCol : req.getGroupByColumns()) {
                    newRow.put(groupCol, exampleRow.get(groupCol));
                }

                for (Map.Entry<String, String> aggEntry : req.getAggregationMapping().entrySet()) {
                    String column = aggEntry.getKey();
                    String function = aggEntry.getValue();

                    validateDataTypes(groupRows, column, function);

                   try {
                        switch (function.toUpperCase()) {
                            case "SUM" -> newRow.put(column, sum(groupRows, column));
                            case "AVG" -> newRow.put(column, avg(groupRows, column));
                            case "MIN" -> newRow.put(column, min(groupRows, column));
                            case "MAX" -> newRow.put(column, max(groupRows, column));
                            case "STDDEV" -> newRow.put(column, stddev(groupRows, column));
                            case "CONCAT" -> newRow.put(column, concat(groupRows, column));
                            case "MODE" -> newRow.put(column, mode(groupRows, column));
                            default -> throw new NodeExecutionException("❌ Group By: Unsupported aggregation function '" + function + "'");
                        }
                    } catch (NumberFormatException | ClassCastException ex) {
                        throw new NodeExecutionException("❌ Group By: Cannot apply '" + function + "' on non-numeric column '" + column + "'");
                    }
                }

                result.add(newRow);
            }

            return result;

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (IllegalArgumentException e) {
            throw new NodeExecutionException("❌ Group By: " + e.getMessage());

        } catch (Exception e) {
            log.error("Group By execution failed", e);
            throw new NodeExecutionException("❌ Group By: Unknown error.");
        }
    }

    private String createGroupKey(Map<String, Object> row, List<String> groupByColumns) {
        return groupByColumns.stream()
                .map(col -> Objects.toString(row.get(col), "NULL"))
                .collect(Collectors.joining("__"));
    }

    private double toDouble(Object obj) {
        if (obj == null)
            return 0.0;
        if (obj instanceof Number num)
            return num.doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double sum(List<Map<String, Object>> rows, String column) {
        return rows.stream()
                .mapToDouble(r -> toDouble(r.get(column)))
                .sum();
    }

    private double avg(List<Map<String, Object>> rows, String column) {
        return rows.stream()
                .mapToDouble(r -> toDouble(r.get(column)))
                .average()
                .orElse(0.0);
    }

    private double min(List<Map<String, Object>> rows, String column) {
        return rows.stream()
                .mapToDouble(r -> toDouble(r.get(column)))
                .min()
                .orElse(0.0);
    }

    private double max(List<Map<String, Object>> rows, String column) {
        return rows.stream()
                .mapToDouble(r -> toDouble(r.get(column)))
                .max()
                .orElse(0.0);
    }

    private double stddev(List<Map<String, Object>> rows, String column) {
        List<Double> values = rows.stream()
                .map(r -> toDouble(r.get(column)))
                .toList();
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> (v - mean) * (v - mean))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private String concat(List<Map<String, Object>> rows, String column) {
        return rows.stream()
                .map(r -> Objects.toString(r.get(column), ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private Object mode(List<Map<String, Object>> rows, String column) {
        Map<Object, Long> frequency = rows.stream()
                .map(r -> r.get(column))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        return frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void validateDataTypes(List<Map<String, Object>> rows, String column, String function) {
        if (isNumericFunction(function)) {
            for (Map<String, Object> row : rows) {
                Object value = row.get(column);
                if (value != null && !(value instanceof Number)) {
                    try {
                        Double.parseDouble(value.toString());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "Field '" + column + "' must be numeric for aggregation " + function);
                    }
                }
            }
        }
    }

    private boolean isNumericFunction(String function) {
        return Set.of("SUM", "AVG", "MIN", "MAX", "STDDEV").contains(function.toUpperCase());
    }
}