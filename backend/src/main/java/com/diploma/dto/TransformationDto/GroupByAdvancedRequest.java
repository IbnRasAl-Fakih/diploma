package com.diploma.dto.TransformationDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GroupByAdvancedRequest {
    private List<Map<String, Object>> data;
    private List<String> groupByColumns; 
    private Map<String, String> aggregationMapping; 
}
