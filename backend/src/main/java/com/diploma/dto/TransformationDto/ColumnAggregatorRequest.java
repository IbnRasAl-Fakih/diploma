package com.diploma.dto.TransformationDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ColumnAggregatorRequest {
    private List<Map<String, Object>> data; // Список строк
    private List<String> columns; // Какие колонки агрегировать
    private String function; // SUM, AVG, MIN, MAX, COUNT, CONCAT
    private String outputColumnName; // Имя нового столбца
}
