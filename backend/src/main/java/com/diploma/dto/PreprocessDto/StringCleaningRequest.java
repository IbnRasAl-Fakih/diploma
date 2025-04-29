package com.diploma.dto.PreprocessDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StringCleaningRequest {
    private List<Map<String, Object>> data;
    private Map<String, List<String>> columnActions;
    private List<String> globalActions;
    private List<String> customCharsToRemove;
}
