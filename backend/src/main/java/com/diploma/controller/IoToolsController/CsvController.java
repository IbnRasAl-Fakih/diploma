package com.diploma.controller.IoToolsController;

import com.diploma.dto.IoToolsDto.CsvReaderRequest;
import com.diploma.service.IoToolsService.CsvReadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/csv")
@Tag(name = "CSV Controller", description = "Контроллер для загрузки и обработки CSV-файлов")
public class CsvController {

    @Operation(summary = "Загрузка и обработка CSV-файла (результат как JSON)")
    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<List<Map<String, Object>>> uploadCsvAsJson(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "request", required = false) @RequestBody(description = "JSON с параметрами разделителя") CsvReaderRequest request) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            String delimiter = request != null && request.getSplit() != null ? request.getSplit() : ",";
            List<Map<String, Object>> data = CsvReadService.readCsvAsJson(file, delimiter);

            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
