package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnSplitterDto.ColumnSplitterRequest;
import com.diploma.dto.TransformationDto.ColumnSplitterDto.ColumnSplitterResponse;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ColumnSplitterService {

    public ColumnSplitterResponse split(ColumnSplitterRequest req) {
        List<Map<String, Object>> selected = new ArrayList<>();
        List<Map<String, Object>> unselected = new ArrayList<>();

        for (Map<String, Object> row : req.getData()) {
            Map<String, Object> sel = new LinkedHashMap<>();
            Map<String, Object> unsel = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (req.getSelectedColumns().contains(entry.getKey())) {
                    sel.put(entry.getKey(), entry.getValue());
                } else {
                    unsel.put(entry.getKey(), entry.getValue());
                }
            }

            selected.add(sel);
            unselected.add(unsel);
        }

        return new ColumnSplitterResponse(selected, unselected);
    }
}
