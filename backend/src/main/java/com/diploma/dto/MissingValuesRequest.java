package com.diploma.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MissingValuesRequest {
    private List<List<String>> data;
    private Map<String, String> actions;
    private Map<String, String> fixValues;
}
