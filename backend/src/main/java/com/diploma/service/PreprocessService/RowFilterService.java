package com.diploma.service.PreprocessService;

import com.diploma.dto.PreprocessDto.RowFilterRequest;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.function.Predicate;


@Service
@NodeType("row_filter")
public class RowFilterService implements NodeExecutor {
    Logger logger = LogManager.getLogger(RowFilterService.class);
    private final ResultService resultService;

    public RowFilterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("Row Filter требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = node.getInputs().get(0).getNodeId();

        List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

        RowFilterRequest request = new RowFilterRequest();
        request.setData(data);
        request.setColumn((String) node.getFields().get("column"));
        request.setOperator((String) node.getFields().get("operator"));
        request.setValue((String) node.getFields().get("value"));
        request.setCaseSensitive((Boolean) node.getFields().get("caseSensitive"));
        request.setExcludeMatches((Boolean) node.getFields().get("excludeMatches"));

        List<Map<String, Object>> filtred = filterRows(request);

        return Map.of("filtred", filtred);
    }

    public List<Map<String, Object>> filterRows(RowFilterRequest req) {
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
                    logger.error("Unknown operator: " + req.getOperator());
                    return false;
            }
        };

        if (req.isExcludeMatches()) {
            predicate = predicate.negate(); 
        }

        return req.getData().stream().filter(predicate).toList();
    }

    private int compareAsNumbers(String a, String b) {
        try {
            return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }
}