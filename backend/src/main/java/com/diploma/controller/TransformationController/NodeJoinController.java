package com.diploma.controller.TransformationController;

import com.diploma.dto.TransformationDto.NodeJoinRequest;
import com.diploma.service.TransformationService.NodeJoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NodeJoinController {

    @Autowired
    private NodeJoinService nodeJoinService;

    @PostMapping("/join")
    public ResponseEntity<List<Map<String, Object>>> joinTables(@RequestBody NodeJoinRequest request) {
        try {
            List<Map<String, Object>> joinedData = nodeJoinService.joinTables(request);
            return ResponseEntity.ok(joinedData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(List.of(Map.of("status", "error", "message", e.getMessage())));
        }
    }
}
