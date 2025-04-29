package com.diploma.controller.IoToolsController;

import com.diploma.dto.IoToolsDto.ExcelReaderRequest;
import com.diploma.service.IoToolsService.ExcelReadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@Tag(name = "Excel Controller", description = "Контроллер для загрузки и обработки Excel-файлов")
public class ExcelReaderController {

    @Operation(summary = "Загрузка и обработка Excel-файла")
    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<List<Map<String, Object>>> uploadFile(
            @RequestPart("file") @Parameter(description = "Excel-файл для загрузки", required = true) MultipartFile file,
            @RequestPart(value = "request", required = false) @RequestBody(description = "JSON с параметрами выбора листа") ExcelReaderRequest request) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<Map<String, Object>> data = ExcelReadService.readExcel(file, request);
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
