package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnAppenderRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ColumnAppenderService {

    public List<Map<String, Object>> appendColumns(ColumnAppenderRequest req) {
        List<Map<String, Object>> a = req.getTableA();
        List<Map<String, Object>> b = req.getTableB();

        if (a.size() != b.size()) {
            throw new IllegalArgumentException("Tables must have the same number of rows.");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < a.size(); i++) {
            Map<String, Object> rowA = a.get(i);
            Map<String, Object> rowB = b.get(i);

            Map<String, Object> merged = new LinkedHashMap<>();
            merged.putAll(rowA);
            merged.putAll(rowB); 

            result.add(merged);
        }

        return result;
    }
}
