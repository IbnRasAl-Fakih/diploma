package com.diploma.dto.PreprocessDto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DuplicateRemoverRequest {
    private List<Map<String,Object>> data;
    private List<String> selectedColumns;
}
