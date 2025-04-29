package com.diploma.service.PreprocessService;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ColumnFilterService {

    public List<Map<String, Object>> removeColumns(List<Map<String, Object>> rows, Set<String> columnsToRemove) {
        return rows.stream()
                .map(row -> {
                    Map<String, Object> filtered = new LinkedHashMap<>(row);
                    columnsToRemove.forEach(filtered::remove);
                    return filtered;
                })
                .collect(Collectors.toList());
    }
}
