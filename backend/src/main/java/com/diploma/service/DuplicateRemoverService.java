package com.diploma.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class DuplicateRemoverService {
    public Map<String, Object> removeDuplicates(List<List<String>> data, List<String> selectedColumns) {
        if (data.isEmpty()) {
            return Map.of("cleanData", List.of(), "duplicates", List.of());
        }

        List<String> headers = data.get(0);

        // Находим индексы выбранных колонок
        List<Integer> columnIndexes = selectedColumns.stream()
                .map(headers::indexOf)
                .filter(index -> index != -1)
                .collect(Collectors.toList());

        Set<List<String>> seen = new HashSet<>();
        List<List<String>> cleanData = new ArrayList<>();
        List<List<String>> duplicates = new ArrayList<>();

        cleanData.add(headers); // Заголовки остаются

        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            List<String> filteredRow = columnIndexes.stream()
                    .map(row::get)
                    .collect(Collectors.toList());

            if (!seen.add(filteredRow)) {
                duplicates.add(row);
            } else {
                cleanData.add(row);
            }
        }

        return Map.of("cleanData", cleanData, " ", duplicates);
    }
}
