package com.diploma.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class GetRequestDTO {
    private String url;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private int timeout;
    private UUID nodeId;
    private UUID workflowId;
}