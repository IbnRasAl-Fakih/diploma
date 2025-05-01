package com.diploma.dto.TransformationDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ColumnRenamerRequest {
    private List<Map<String, Object>> data;
    private Map<String, String> renameMap; 
}
