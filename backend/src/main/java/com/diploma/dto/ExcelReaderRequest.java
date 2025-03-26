package com.diploma.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelReaderRequest {
    private String sheetName; 
    private int sheetPosition;
}