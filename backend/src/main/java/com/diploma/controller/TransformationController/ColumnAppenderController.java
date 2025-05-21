package com.diploma.controller.TransformationController;

import com.diploma.dto.TransformationDto.ColumnAppenderRequest;
import com.diploma.service.TransformationService.ColumnAppenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/columns")
public class ColumnAppenderController {

    private final ColumnAppenderService columnAppenderService;

    public ColumnAppenderController(ColumnAppenderService columnAppenderService) {
        this.columnAppenderService = columnAppenderService;
    }

    @PostMapping("/append")
    public ResponseEntity<List<Map<String, Object>>> append(@RequestBody ColumnAppenderRequest req) throws Exception {
        return ResponseEntity.ok(columnAppenderService.appendColumns(req));
    }
}