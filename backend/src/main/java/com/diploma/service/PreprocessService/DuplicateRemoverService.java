package com.diploma.service.PreprocessService;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateRemoverService {
    public Map<String, Object> removeDuplicates(List<Map<String, Object>> data, List<String> selectedColumns) {
        if (data == null || data.isEmpty()) {
            return Map.of("cleanData", List.of(), "duplicates", List.of());
        }

        Set<String> allKeys = data.get(0).keySet();

        // Храним уже встреченные записи по выбранным колонкам
        Set<List<Object>> seen = new HashSet<>();
        List<Map<String, Object>> cleanData = new ArrayList<>();
        List<Map<String, Object>> duplicates = new ArrayList<>();

        for (Map<String, Object> row : data) {
            List<Object> key = selectedColumns.stream()
                    .map(row::get)
                    .collect(Collectors.toList());

            if (!seen.add(key)) {
                duplicates.add(row);
            } else {
                cleanData.add(row);
            }
        }

        return Map.of(
                "cleanData", cleanData,
                "duplicates", duplicates);
    }
}
