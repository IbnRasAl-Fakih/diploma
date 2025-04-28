package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnAggregatorRequest;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ColumnAggregatorService {

    public List<Map<String, Object>> aggregateColumns(ColumnAggregatorRequest req) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> row : req.getData()) {
            Map<String, Object> newRow = new LinkedHashMap<>(row);

            switch (req.getFunction().toUpperCase()) {
                case "SUM" -> newRow.put(req.getOutputColumnName(), sum(row, req.getColumns()));
                case "AVG" -> newRow.put(req.getOutputColumnName(), avg(row, req.getColumns()));
                case "MIN" -> newRow.put(req.getOutputColumnName(), min(row, req.getColumns()));
                case "MAX" -> newRow.put(req.getOutputColumnName(), max(row, req.getColumns()));
                case "COUNT" -> newRow.put(req.getOutputColumnName(), count(row, req.getColumns()));
                case "CONCAT" -> newRow.put(req.getOutputColumnName(), concat(row, req.getColumns()));
                default -> throw new IllegalArgumentException("Unsupported aggregation function: " + req.getFunction());
            }

            result.add(newRow);
        }

        return result;
    }

    private double sum(Map<String, Object> row, List<String> columns) {
        return columns.stream()
                .mapToDouble(col -> toDouble(row.get(col)))
                .sum();
    }

    private double avg(Map<String, Object> row, List<String> columns) {
        return sum(row, columns) / columns.size();
    }

    private double min(Map<String, Object> row, List<String> columns) {
        return columns.stream()
                .mapToDouble(col -> toDouble(row.get(col)))
                .min()
                .orElse(0.0);
    }

    private double max(Map<String, Object> row, List<String> columns) {
        return columns.stream()
                .mapToDouble(col -> toDouble(row.get(col)))
                .max()
                .orElse(0.0);
    }

    private int count(Map<String, Object> row, List<String> columns) {
        return (int) columns.stream()
                .filter(col -> row.get(col) != null)
                .count();
    }

    private String concat(Map<String, Object> row, List<String> columns) {
        return columns.stream()
                .map(col -> Objects.toString(row.get(col), ""))
                .filter(s -> !s.isEmpty()) 
                .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
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
}
