package com.diploma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.diploma.dto.CsvReaderRequest;
import com.diploma.service.CsvReadService;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/csv")
@Tag(name = "CSV Controller", description = "Контроллер для загрузки и обработки CSV-файлов")
public class CSVcontroller {
    @Operation(summary = "Загрузка и обработка CSV-файла")
    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })

    public ResponseEntity<List<List<String>>> uploadCsv(
        @RequestPart("file") MultipartFile file,
        @RequestPart(value = "request", required = false) @RequestBody(description = "JSON с параметрами разделителя") CsvReaderRequest request) {      
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            List<List<String>> data = CsvReadService.readCsv(file, request.getSplit());
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
