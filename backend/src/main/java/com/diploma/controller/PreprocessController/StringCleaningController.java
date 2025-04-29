package com.diploma.controller.PreprocessController;

import com.diploma.dto.PreprocessDto.StringCleaningRequest;
import com.diploma.service.PreprocessService.StringCleaningService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clean")
@Tag(name = "String Cleaning Controller", description = "Контроллер для очистки строковых данных")
public class StringCleaningController {

    private final StringCleaningService stringCleaningService;

    public StringCleaningController(StringCleaningService stringCleaningService) {
        this.stringCleaningService = stringCleaningService;
    }

    @Operation(summary = "Очистка строк от пробелов, непечатаемых и пользовательских символов")
    @PostMapping("/strings")
    public ResponseEntity<List<Map<String, Object>>> cleanStrings(@RequestBody StringCleaningRequest request) {
        List<Map<String, Object>> cleanedData = stringCleaningService.cleanStrings(request);
        return ResponseEntity.ok(cleanedData);
    }
}
