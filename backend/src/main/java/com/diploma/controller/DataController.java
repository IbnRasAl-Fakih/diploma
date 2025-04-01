package com.diploma.controller;

import com.diploma.dto.MissingValuesRequest;
import com.diploma.service.DataProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@Tag(name = "Data Controller", description = "Обработка пропущенных значений в данных")
public class DataController {

    private final DataProcessingService dataProcessingService;

    public DataController(DataProcessingService dataProcessingService) {
        this.dataProcessingService = dataProcessingService;
    }

    @Operation(summary = "Заполнить пропущенные значения в JSON-данных")
    @PostMapping("/process")
    public ResponseEntity<List<List<String>>> processMissingValues(@RequestBody MissingValuesRequest request) {
        List<List<String>> processedData = dataProcessingService.processMissingValues(request);
        return ResponseEntity.ok(processedData);
    }
}
