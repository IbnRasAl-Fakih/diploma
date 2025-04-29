package com.diploma.controller.PreprocessController;

import com.diploma.dto.PreprocessDto.RowFilterRequest;
import com.diploma.service.PreprocessService.RowFilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rows")
public class RowFilterController {

    private final RowFilterService rowFilterService;

    public RowFilterController(RowFilterService rowFilterService) {
        this.rowFilterService = rowFilterService;
    }

    @PostMapping("/filter")
    public ResponseEntity<List<Map<String, Object>>> filter(@RequestBody RowFilterRequest req) {
        return ResponseEntity.ok(rowFilterService.filterRows(req));
    }
}
