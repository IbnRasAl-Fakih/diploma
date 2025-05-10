package com.diploma.dto.DbToolsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseConnectionRequest {
    private String databaseType;
    private String url;
    private String username;
    private String password;
    private String driver;
}