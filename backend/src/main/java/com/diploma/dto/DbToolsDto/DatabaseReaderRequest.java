package com.diploma.dto.DbToolsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseReaderRequest {
    private String sessionId;
    private String statement;
}