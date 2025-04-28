package com.diploma.dto.PreprocessDto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ColumnFilterRequest {
    private List<Map<String, Object>> data; // Исходные строки
    private Set<String> columnsToRemove; // Колонки, которые нужно удалить
}
