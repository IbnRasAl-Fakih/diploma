package com.diploma.dto.IoToolsDto.ExcelReader;

import lombok.Data;

import java.util.UUID;

@Data
public class ExcelExecutionRequest {
    private UUID workflowId;
    private String type;
    private UUID nodeId;
    private ExcelReaderRequest readerParams; 
}
