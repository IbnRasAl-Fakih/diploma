package com.diploma.service.IoToolsService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.diploma.dto.IoToolsDto.ExcelReader.ExcelReaderRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ExcelReadService {

    public static List<Map<String, Object>> readExcel(MultipartFile file, ExcelReaderRequest request)
            throws IOException {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {

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

            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return Collections.emptyList();
            }

            // Чтение заголовков
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }


            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData.put(headers.get(j), getCellValue(cell));
                }
                result.add(rowData);
            }

            return result;
        }
    }

    private static Object getCellValue(Cell cell) {
        if (cell == null)
            return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            case BLANK -> null;
            default -> cell.toString();
        };
    }
}
