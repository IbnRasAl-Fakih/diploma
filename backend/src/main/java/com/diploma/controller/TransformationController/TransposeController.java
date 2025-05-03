package com.diploma.controller.TransformationController;

import com.diploma.dto.TransformationDto.TransposeRequest;
import com.diploma.service.TransformationService.TransposeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/columns/transpose")
public class TransposeController {

    private final TransposeService transposeService;

    public TransposeController(TransposeService transposeService) {
        this.transposeService = transposeService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> transpose(@RequestBody TransposeRequest req) {
        return ResponseEntity.ok(transposeService.transpose(req));
    }
}
