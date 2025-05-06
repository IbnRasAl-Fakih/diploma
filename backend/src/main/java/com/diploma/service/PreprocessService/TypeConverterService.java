package com.diploma.service.PreprocessService;

import org.springframework.stereotype.Service;

import com.diploma.dto.PreprocessDto.TypeConverterRequest;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TypeConverterService {

private static final List<DateTimeFormatter> dateFormats = List.of(
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
);

    public List<Map<String, Object>> convertTypes(TypeConverterRequest request) {
        List<Map<String, Object>> data = request.getData();
        Map<String, String> columnTypes = request.getColumnTypes();

        for (Map<String, Object> row : data) {
            for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
                String column = entry.getKey();
                String type = entry.getValue().toLowerCase();

                Object value = row.get(column);
                if (value != null) {
                    try {
                        Object convertedValue = convertValue(value, type);
                        row.put(column, convertedValue);
                    } catch (Exception e) {
                        row.put(column, null); 
                    }
                }
            }
        }

        return data;
    }

    private Object convertValue(Object value, String type) throws ParseException {
        switch (type) {
            case "string":
                return value.toString();

            case "integer":
                String cleanedInt = value.toString().replaceAll("[^\\d-]", "");
                return Long.parseLong(cleanedInt);

            case "double":
                String cleanedDouble = value.toString().replaceAll("[^\\d.,-]", "").replace(",", ".");
                return Double.parseDouble(cleanedDouble);

            case "boolean":
                return Boolean.parseBoolean(value.toString());

            case "date":
            for (DateTimeFormatter formatter : dateFormats) {
                try {
                    LocalDate localDate = LocalDate.parse(value.toString(), formatter);
                    ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault()); // например, Asia/Almaty
                    return Date.from(zdt.toInstant());
                } catch (DateTimeParseException ignored) {}
            }
            throw new IllegalArgumentException("Unrecognized date: " + value);

            default:
                return value.toString();
        }
    }
}
