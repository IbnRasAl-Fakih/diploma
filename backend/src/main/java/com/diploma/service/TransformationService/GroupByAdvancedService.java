package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.GroupByAdvancedRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupByAdvancedService {

    public List<Map<String, Object>> groupBy(GroupByAdvancedRequest req) {
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

                switch (function.toUpperCase()) {
                    case "SUM" -> newRow.put(column, sum(groupRows, column));
                    case "AVG" -> newRow.put(column, avg(groupRows, column));
                    case "MIN" -> newRow.put(column, min(groupRows, column));
                    case "MAX" -> newRow.put(column, max(groupRows, column));
                    case "STDDEV" -> newRow.put(column, stddev(groupRows, column));
                    case "CONCAT" -> newRow.put(column, concat(groupRows, column));
                    case "MODE" -> newRow.put(column, mode(groupRows, column));
                    default -> throw new IllegalArgumentException("Unsupported aggregation function: " + function);
                }
            }

            result.add(newRow);
        }

        return result;
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