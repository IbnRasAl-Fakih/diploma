package com.diploma.service.IoToolsService;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CsvReadService {

    public static List<Map<String, Object>> readCsvAsJson(MultipartFile file, String delimiter) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = br.readLine();
            if (headerLine == null)
                return records;

            String[] headers = headerLine.split(delimiter);

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter, -1); // -1 сохраняет пустые значения
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < values.length ? values[i].trim() : null;
                    row.put(key, parseValue(value));
                }
                records.add(row);
            }
        }

        return records;
    }

    private static Object parseValue(String value) {
        if (value == null || value.isEmpty())
            return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        return value; 
    }
}
