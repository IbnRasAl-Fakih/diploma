package com.diploma.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequestDto {
    private UUID ownerId;
    private Map<String, Object> structure;
}