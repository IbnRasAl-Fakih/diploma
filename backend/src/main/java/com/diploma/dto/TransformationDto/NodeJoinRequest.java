package com.diploma.dto.TransformationDto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NodeJoinRequest {
    private List<Map<String, Object>> tableA; 
    private List<Map<String, Object>> tableB; 
    private String joinColumn; 
}