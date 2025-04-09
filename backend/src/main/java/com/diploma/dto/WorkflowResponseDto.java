package com.diploma.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponseDto {
    private UUID id;
    private UUID ownerId;
    private Map<String, Object> structure;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
