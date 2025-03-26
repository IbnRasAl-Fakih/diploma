package com.diploma.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.diploma.dto.ExcelReaderRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@Tag(name = "Excel Controller", description = "Контроллер для загрузки и обработки Excel-файлов")
public class ExcelReaderController {

    @Operation(summary = "Загрузка и обработка Excel-файла")
    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<List<List<String>>> uploadFile(
            @RequestPart("file") @Parameter(description = "Excel-файл для загрузки", required = true) MultipartFile file,
            @RequestPart(value = "request", required = false) @RequestBody(description = "JSON с параметрами выбора листа") ExcelReaderRequest request) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet;
            if (request != null) {
                if (request.getSheetName() != null && !request.getSheetName().isEmpty()) {
                    sheet = workbook.getSheet(request.getSheetName());
                } else if (request.getSheetPosition() >= 0) {
                    sheet = workbook.getSheetAt(request.getSheetPosition());
                } else {
                    sheet = workbook.getSheetAt(0);
                }
            } else {
                sheet = workbook.getSheetAt(0);
            }

            if (sheet == null) {
                return ResponseEntity.badRequest().body(null);
            }

            List<List<String>> data = new ArrayList<>();
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(cell.toString());
                }
                data.add(rowData);
            }

            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
