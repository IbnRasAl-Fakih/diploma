package com.diploma.dto.TransformationDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NodeSorterRequest {
    private List<String> columns; 
    private boolean ascending; 
    private List<Map<String, Object>> data;
}
