package com.diploma.controller.PreprocessController;

import com.diploma.dto.PreprocessDto.ColumnFilterRequest;
import com.diploma.service.PreprocessService.ColumnFilterService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/columns")
public class ColumnFilterController {

    private final ColumnFilterService columnFilterService;

    public ColumnFilterController(ColumnFilterService columnFilterService) {
        this.columnFilterService = columnFilterService;
    }

    @PostMapping("/filter")
    public ResponseEntity<List<Map<String, Object>>> filterColumns(@RequestBody ColumnFilterRequest request) {
        List<Map<String, Object>> result = columnFilterService.removeColumns(request.getData(),
                request.getColumnsToRemove());
        return ResponseEntity.ok(result);
    }
}
