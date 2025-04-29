package com.diploma.dto.PreprocessDto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MissingValuesRequest {
    private List<Map<String, Object>> data;
    private Map<String, String> actions;
    private Map<String, Object> fixValues;
}
