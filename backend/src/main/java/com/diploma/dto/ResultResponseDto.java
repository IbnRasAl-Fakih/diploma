package com.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultResponseDto {
    private UUID nodeId;
    private String type;
    private UUID workflowId;
    private Map<String, Object> result;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
