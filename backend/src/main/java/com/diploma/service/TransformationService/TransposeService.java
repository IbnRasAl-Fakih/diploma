package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.TransposeRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransposeService {

    public List<Map<String, Object>> transpose(TransposeRequest req) {
        List<Map<String, Object>> rows = req.getData();
        if (rows == null || rows.isEmpty())
            return Collections.emptyList();

        List<String> rowKeys = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            rowKeys.add("row_" + i);
        }

        Set<String> columnNames = rows.get(0).keySet(); 

        List<Map<String, Object>> transposed = new ArrayList<>();

        for (String col : columnNames) {
            Map<String, Object> newRow = new LinkedHashMap<>();
            newRow.put("ColumnHeader", col);

            for (int i = 0; i < rows.size(); i++) {
                Object value = rows.get(i).get(col);
                newRow.put("row_" + i, value);
            }

            transposed.add(newRow);
        }

        return transposed;
    }
}
