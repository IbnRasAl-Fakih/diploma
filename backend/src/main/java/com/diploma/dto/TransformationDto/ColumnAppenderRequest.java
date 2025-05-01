package com.diploma.dto.TransformationDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ColumnAppenderRequest {
    private List<Map<String, Object>> tableA;
    private List<Map<String, Object>> tableB;
}
