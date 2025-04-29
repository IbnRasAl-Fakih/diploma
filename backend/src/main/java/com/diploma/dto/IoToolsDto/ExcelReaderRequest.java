package com.diploma.dto.IoToolsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelReaderRequest {
    private String sheetName; 
    private int sheetPosition;
}