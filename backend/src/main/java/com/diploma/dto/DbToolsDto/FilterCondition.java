package com.diploma.dto.DbToolsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterCondition {
    private String column;
    private String operator;
    private String value;
}