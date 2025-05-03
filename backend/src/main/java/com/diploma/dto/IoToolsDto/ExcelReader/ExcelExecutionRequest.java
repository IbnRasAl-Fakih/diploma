package com.diploma.dto.IoToolsDto.ExcelReader;

import lombok.Data;

import java.util.UUID;

@Data
public class ExcelExecutionRequest {
    private UUID workflowId;
    private UUID nodeId;
    private ExcelReaderRequest readerParams; 
}
