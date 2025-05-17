package com.diploma.dto.DbToolsDto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DbRowFilterRequest {
    private String sessionId;
    private String tableName;
    private List<FilterCondition> filters;
}