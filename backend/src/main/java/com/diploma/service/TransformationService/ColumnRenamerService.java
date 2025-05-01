package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnRenamerRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ColumnRenamerService {

    public List<Map<String, Object>> renameColumns(ColumnRenamerRequest req) {
        List<Map<String, Object>> renamed = new ArrayList<>();

        for (Map<String, Object> row : req.getData()) {
            Map<String, Object> newRow = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String originalKey = entry.getKey();
                String renamedKey = req.getRenameMap().getOrDefault(originalKey, originalKey);
                newRow.put(renamedKey, entry.getValue());
            }

            renamed.add(newRow);
        }

        return renamed;
    }
}
