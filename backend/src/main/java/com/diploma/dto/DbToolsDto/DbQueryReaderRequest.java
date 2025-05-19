package com.diploma.dto.DbToolsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbQueryReaderRequest {
    private String sessionId;
    private String statement;
}