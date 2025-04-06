package com.diploma.dto;

import java.util.List;
import lombok.Data;

@Data
public class DeduplicationRequest {
    private List<List<String>> data;
    private List<String> selectedColumns;
}
