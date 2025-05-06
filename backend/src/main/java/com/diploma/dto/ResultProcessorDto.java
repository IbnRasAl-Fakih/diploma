package com.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultProcessorDto {
    private UUID nodeId;
    private UUID workflowId;
    private Object result;
}
