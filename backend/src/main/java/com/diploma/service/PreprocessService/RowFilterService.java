package com.diploma.service.PreprocessService;

import com.diploma.dto.PreprocessDto.RowFilterRequest;
import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.function.Predicate;


@Service
@NodeType("row_filter")
public class RowFilterService implements NodeExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(RowFilterService.class);
    private final ResultService resultService;

    public RowFilterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Row Filter: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();

            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Row Filter: Failed to get the result of the previous node.");
            }

            RowFilterRequest request = new RowFilterRequest();
            request.setData(data);
            request.setColumn((String) node.getFields().get("column"));
            request.setOperator((String) node.getFields().get("operator"));
            request.setValue((String) node.getFields().get("value"));
            request.setCaseSensitive((Boolean) node.getFields().get("caseSensitive"));
            request.setExcludeMatches((Boolean) node.getFields().get("excludeMatches"));

            List<Map<String, Object>> filtred = filterRows(request);

            return Map.of("filtred", filtred);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Row Filter execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Row Filter execution failed.");
        }
    }

    public List<Map<String, Object>> filterRows(RowFilterRequest req) throws Exception {
        try {
            if (req.getColumn() == null || req.getColumn().isBlank()) {
                throw new NodeExecutionException("❌ Row Filter: Column name is missing.");
            }

            if (req.getValue() == null) {
                throw new NodeExecutionException("❌ Row Filter: Comparison value is missing.");
            }

            Predicate<Map<String, Object>> predicate = row -> {
                Object val = row.get(req.getColumn());
                if (val == null)
                    return false;

                String target = val.toString();
                String compareTo = req.getValue();

                if (!req.isCaseSensitive()) {
                    target = target.toLowerCase();
                    compareTo = compareTo.toLowerCase();
                }

                switch (req.getOperator()) {
                    case "=":
                        return target.equals(compareTo);
                    case "!=":
                        return !target.equals(compareTo);
                    case "<":
                        return compareAsNumbers(target, compareTo) < 0;
                    case ">":
                        return compareAsNumbers(target, compareTo) > 0;
                    case "<=":
                        return compareAsNumbers(target, compareTo) <= 0;
                    case ">=":
                        return compareAsNumbers(target, compareTo) >= 0;
                    case "contains":
                        return target.contains(compareTo);
                    default:
                        throw new NodeExecutionException("❌ Row Filter: Unknown operator '" + req.getOperator() + "'");
                }
            };

            if (req.isExcludeMatches()) {
                predicate = predicate.negate(); 
            }

            return req.getData().stream().filter(predicate).toList();

        } catch (NodeExecutionException e) {
            throw e;

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Row Filter: Invalid row data format.");

        } catch (Exception e) {
            log.error("Row Filter execution failed", e);
            throw new NodeExecutionException("❌ Row Filter: Unknown error.");
        }
    }

    private int compareAsNumbers(String a, String b) {
        try {
            Double d1 = Double.parseDouble(a);
            Double d2 = Double.parseDouble(b);
            return Double.compare(d1, d2);
        } catch (NumberFormatException e) {
            throw new NodeExecutionException("❌ Row Filter: Cannot compare non-numeric values: '" + a + "' and '" + b + "'");
        }
    }
}