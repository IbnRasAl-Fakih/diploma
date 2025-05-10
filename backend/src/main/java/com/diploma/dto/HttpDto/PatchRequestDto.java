package com.diploma.dto.HttpDto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PatchRequestDto {
    private String url;
    private Map<String, String> headers;
    private List<Map<String, Object>> body;
    private int timeout;
}