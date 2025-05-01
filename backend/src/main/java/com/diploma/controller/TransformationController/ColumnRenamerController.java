package com.diploma.controller.TransformationController;

import com.diploma.dto.TransformationDto.ColumnRenamerRequest;
import com.diploma.service.TransformationService.ColumnRenamerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/columns/rename")
public class ColumnRenamerController {

    private final ColumnRenamerService columnRenamerService;

    public ColumnRenamerController(ColumnRenamerService columnRenamerService) {
        this.columnRenamerService = columnRenamerService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> rename(@RequestBody ColumnRenamerRequest req) {
        return ResponseEntity.ok(columnRenamerService.renameColumns(req));
    }
}
