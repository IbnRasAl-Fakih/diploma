package com.diploma.controller.PreprocessController;

import org.springframework.web.bind.annotation.*;

import com.diploma.dto.PreprocessDto.DuplicateRemoverRequest;
import com.diploma.service.PreprocessService.DuplicateRemoverService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DuplicateRemoverController {
    private final DuplicateRemoverService duplicateRemoverService;

    public DuplicateRemoverController(DuplicateRemoverService duplicateRemoverService) {
        this.duplicateRemoverService = duplicateRemoverService;
    }

    @PostMapping("/remove-duplicates")
    public List<Map<String, Object>> removeDuplicates(@RequestBody DuplicateRemoverRequest request) throws Exception {
        return duplicateRemoverService.removeDuplicates(request.getData(), request.getSelectedColumns());
    }
}