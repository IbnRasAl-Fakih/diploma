package com.diploma.service;

import com.diploma.dto.MissingValuesRequest;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;

@Service
public class DataProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessingService.class);

    public List<List<String>> processMissingValues(MissingValuesRequest request) {
        List<List<String>> data = request.getData();
        Map<String, String> actions = request.getActions();
        Map<String, String> fixValues = request.getFixValues();

        if (data == null || data.isEmpty()) {
            return data;
        }

        List<String> headers = data.get(0); // Заголовки колонок

        for (int rowData = 1; rowData < data.size(); rowData++) { // Начинаем со второй строки, так как первая — заголовки
            List<String> row = data.get(rowData);
            for (int j = 0; j < headers.size(); j++) {
                if (row.get(j) == null) {
                    String column = headers.get(j);
                    String action = actions.getOrDefault(column, "Skip");

                    switch (action) {
                        case "Fix Value":
                            row.set(j, fixValues.getOrDefault(column, "0"));
                            break;
                        case "Most Frequent Value":
                            row.set(j, getMostFrequentValue(data, j));
                            break;
                        case "Next Value":
                            row.set(j, getNextValue(data, rowData, j));
                            break;
                        case "Previous Value":
                            row.set(j, getPreviousValue(data, rowData, j));
                            break;
                        case "Remove Row":
                            row.clear();
                            break;
                        case "Skip":
                        default:
                            break;
                    }
                }
            }
        }
        return data;
    }

    private String getMostFrequentValue(List<List<String>> data, int columnIndex) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            if (columnIndex < row.size() && row.get(columnIndex) != null && !row.get(columnIndex).isEmpty()) {
                frequencyMap.put(row.get(columnIndex), frequencyMap.getOrDefault(row.get(columnIndex), 0) + 1);
            }
        }
        String mostFrequentValue = frequencyMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("0");

        // Логируем частотный словарь и результат
        logger.info("Самое частое значение в столбце {}: {}", columnIndex, mostFrequentValue);

        return mostFrequentValue;
    }

    private String getNextValue(List<List<String>> data, int rowIndex, int columnIndex) {
        for (int i = rowIndex + 1; i < data.size(); i++) {
            if (columnIndex < data.get(i).size() && !data.get(i).get(columnIndex).isEmpty()) {
                return data.get(i).get(columnIndex);
            }
        }
        return "0";
    }

    private String getPreviousValue(List<List<String>> data, int rowIndex, int columnIndex) {
        for (int i = rowIndex - 1; i > 0; i--) {
            if (columnIndex < data.get(i).size() && !data.get(i).get(columnIndex).isEmpty()) {
                return data.get(i).get(columnIndex);
            }
        }
        return "0";
    }
}
