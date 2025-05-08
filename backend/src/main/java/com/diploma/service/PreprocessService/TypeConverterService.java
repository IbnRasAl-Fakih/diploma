package com.diploma.service.PreprocessService;

import org.springframework.stereotype.Service;

import com.diploma.dto.PreprocessDto.TypeConverterRequest;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@NodeType("type_converter")
public class TypeConverterService implements NodeExecutor {
    private final ResultService resultService;

    public TypeConverterService(ResultService resultService) {
        this.resultService = resultService;
    }
    
    @Override
    public Object execute(Map<String, Object> fields, List<String> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("TypeConverter требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = UUID.fromString(inputs.get(0));
        List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

        TypeConverterRequest request = new TypeConverterRequest();
        request.setData(data);
        request.setColumnTypes((Map<String, String>) fields.get("columnTypes"));

        List<Map<String, Object>> converted = convertTypes(request);

        return Map.of("Converted", converted);
    }

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
