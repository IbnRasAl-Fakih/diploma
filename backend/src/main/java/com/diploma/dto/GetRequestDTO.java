package com.diploma.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class GetRequestDTO {
    private String url;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private int timeout;
}