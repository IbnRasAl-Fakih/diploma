package com.diploma.controller.PreprocessController;

import com.diploma.dto.PreprocessDto.MissingValuesRequest;
import com.diploma.service.PreprocessService.MissingDataProcessingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Data Controller", description = "Обработка пропущенных значений в данных")
public class MissingDataController {

    private final MissingDataProcessingService dataProcessingService;

    public MissingDataController(MissingDataProcessingService dataProcessingService) {
        this.dataProcessingService = dataProcessingService;
    }

    @Operation(summary = "Заполнить пропущенные значения в JSON-данных")
    @PostMapping("/missingDataProcess")
    public ResponseEntity<List<Map<String, Object>>> processMissingValues(@RequestBody MissingValuesRequest request) {
        List<Map<String, Object>> processedData = dataProcessingService.processMissingValues(request);
        return ResponseEntity.ok(processedData);
    }
}
