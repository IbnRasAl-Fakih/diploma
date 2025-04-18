package com.diploma.controller;

import com.diploma.dto.GetRequestDTO;
import com.diploma.service.GetRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/http")
public class GetRequestController {

    private final GetRequestService getRequestService;

    public GetRequestController(GetRequestService getRequestService) {
        this.getRequestService = getRequestService;
    }

    @PostMapping("/get")
    public ResponseEntity<String> performGetRequest(@RequestBody GetRequestDTO request) {
        try {
            String response = getRequestService.sendGetRequest(
                    request.getUrl(),
                    request.getHeaders(),
                    request.getQueryParams(),
                    request.getTimeout(),
                    request.getWorkflowId(),
                    request.getNodeId()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error while making GET request: " + e.getMessage());
        }
    }
}
