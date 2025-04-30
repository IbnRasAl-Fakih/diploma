package com.diploma.controller.TransformationController;

import com.diploma.dto.TransformationDto.GroupByAdvancedRequest;
import com.diploma.service.TransformationService.GroupByAdvancedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groupby/advanced")
public class GroupByAdvancedController {

    private final GroupByAdvancedService groupByAdvancedService;

    public GroupByAdvancedController(GroupByAdvancedService groupByAdvancedService) {
        this.groupByAdvancedService = groupByAdvancedService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> groupBy(@RequestBody GroupByAdvancedRequest request) {
        List<Map<String, Object>> result = groupByAdvancedService.groupBy(request);
        return ResponseEntity.ok(result);
    }
}
