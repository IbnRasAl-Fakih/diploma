package com.diploma.dto.PreprocessDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RowFilterRequest {
    private List<Map<String, Object>> data;
    private String column; // колонка для фильтрации
    private String operator; // =, !=, <, >, <=, >=, contains
    private String value; // значение для сравнения
    private boolean caseSensitive; // только для строк 
    private boolean excludeMatches = false; 
}
