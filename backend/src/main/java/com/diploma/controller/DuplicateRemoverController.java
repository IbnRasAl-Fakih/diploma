package com.diploma.controller;

import org.springframework.web.bind.annotation.*;

import com.diploma.service.DuplicateRemoverService;
import com.diploma.dto.DeduplicationRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DuplicateRemoverController {
    private final DuplicateRemoverService duplicateRemoverService;

    public DuplicateRemoverController(DuplicateRemoverService duplicateRemoverService) {
        this.duplicateRemoverService = duplicateRemoverService;
    }

    @PostMapping("/remove-duplicates")
    public Map<String, Object> removeDuplicates(@RequestBody DeduplicationRequest request) {
        return duplicateRemoverService.removeDuplicates(request.getData(), request.getSelectedColumns());
    }
}
