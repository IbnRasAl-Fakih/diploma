package com.diploma.dto.TransformationDto.ColumnSplitterDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ColumnSplitterResponse {
    private List<Map<String, Object>> selected;
    private List<Map<String, Object>> unselected;
}
