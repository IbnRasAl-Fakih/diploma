package com.diploma.dto.DbToolsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbTableSelectorRequest {
    private String sessionId;
    private String tableName;
}