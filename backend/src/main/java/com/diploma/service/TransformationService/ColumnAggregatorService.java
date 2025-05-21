package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnAggregatorRequest;
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
@NodeType("column_aggregator")
public class ColumnAggregatorService implements NodeExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(ColumnAggregatorService.class);
    private final ResultService resultService;

    public ColumnAggregatorService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Column Aggregator: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Column Aggregator: Failed to get the result of the previous node.");
            }

            ColumnAggregatorRequest request = new ColumnAggregatorRequest();
            request.setData(data);
            request.setFunction((String) node.getFields().get("function"));
            request.setColumns((List<String>) node.getFields().get("columns"));
            request.setOutputColumnName((String) node.getFields().get("outputColumnName"));

            List<Map<String, Object>> aggregated = aggregateColumns(request);

            return Map.of("Aggregated", aggregated);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Column Aggregator execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Column Aggregator execution failed.");
        }
    }
    
    public List<Map<String, Object>> aggregateColumns(ColumnAggregatorRequest req) throws Exception {
        try {

            if (req.getFunction() == null || req.getColumns() == null) {
                throw new NodeExecutionException("❌ Column Aggregator: Required fields are null.");
            }

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
                    default -> throw new NodeExecutionException("❌ Column Aggregator: Unsupported function: " + req.getFunction());
                }

                result.add(newRow);
            }

            return result;

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (ClassCastException | NumberFormatException e) {
            throw new NodeExecutionException("❌ Column Aggregator: Type conversion error during aggregation.");

        } catch (IllegalArgumentException e) {
            throw new NodeExecutionException("❌ Column Aggregator: " + e.getMessage());

        } catch (Exception e) {
            log.error("Column Aggregator execution failed", e);
            throw new NodeExecutionException("❌ Column Aggregator: Unknown error.");
        }
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