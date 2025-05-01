package com.diploma.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class WorkflowExecutorRequestDto {
    private UUID workflowId;
    private List<Map<String, Object>> nodes;
}