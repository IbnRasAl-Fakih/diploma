package com.diploma.controller.PreprocessController;

import com.diploma.dto.PreprocessDto.TypeConverterRequest;
import com.diploma.service.PreprocessService.TypeConverterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/type-converter")
public class TypeConverterController {

    private final TypeConverterService typeConverterService;

    public TypeConverterController(TypeConverterService typeConverterService) {
        this.typeConverterService = typeConverterService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> convertTypes(@RequestBody TypeConverterRequest request) {
        List<Map<String, Object>> converted = typeConverterService.convertTypes(request);
        return ResponseEntity.ok(converted);
    }
}

