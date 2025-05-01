package com.diploma.dto.TransformationDto.ColumnSplitterDto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ColumnSplitterRequest {
    private List<Map<String, Object>> data; 
    private Set<String> selectedColumns; 
}
