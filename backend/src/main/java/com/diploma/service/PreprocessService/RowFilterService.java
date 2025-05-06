package com.diploma.service.PreprocessService;

import com.diploma.dto.PreprocessDto.RowFilterRequest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.function.Predicate;


@Service
public class RowFilterService {
    Logger logger = LogManager.getLogger(RowFilterService.class);
    
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
