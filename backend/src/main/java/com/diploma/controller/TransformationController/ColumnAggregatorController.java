package com.diploma.controller.TransformationController;

import com.diploma.dto.TransformationDto.ColumnAggregatorRequest;
import com.diploma.service.TransformationService.ColumnAggregatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/columns")
public class ColumnAggregatorController {

    private final ColumnAggregatorService columnAggregatorService;

    public ColumnAggregatorController(ColumnAggregatorService columnAggregatorService) {
        this.columnAggregatorService = columnAggregatorService;
    }

    @PostMapping("/aggregate")
    public ResponseEntity<List<Map<String, Object>>> aggregateColumns(@RequestBody ColumnAggregatorRequest request) {
        List<Map<String, Object>> result = columnAggregatorService.aggregateColumns(request);
        return ResponseEntity.ok(result);
    }
}
