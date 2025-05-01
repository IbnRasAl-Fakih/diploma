package com.diploma.controller.TransformationController;

import com.diploma.dto.TransformationDto.ColumnSplitterDto.ColumnSplitterRequest;
import com.diploma.dto.TransformationDto.ColumnSplitterDto.ColumnSplitterResponse;
import com.diploma.service.TransformationService.ColumnSplitterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/columns/split")
public class ColumnSplitterController {

    private final ColumnSplitterService columnSplitterService;

    public ColumnSplitterController(ColumnSplitterService columnSplitterService) {
        this.columnSplitterService = columnSplitterService;
    }

    @PostMapping
    public ResponseEntity<ColumnSplitterResponse> split(@RequestBody ColumnSplitterRequest req) {
        return ResponseEntity.ok(columnSplitterService.split(req));
    }
}
