package com.diploma.dto.DbToolsDto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DbTableCreatorRequest {
    private String sessionId;
    private String tableName;
    private List<Map<String, String>> columns;
}