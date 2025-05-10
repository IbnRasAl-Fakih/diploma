package com.diploma.controller.HttpController;

import com.diploma.dto.HttpDto.PutRequestDto;
import com.diploma.service.HttpService.PutRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/http")
public class PutRequestController {

    private final PutRequestService putRequestService;

    public PutRequestController(PutRequestService putRequestService) {
        this.putRequestService = putRequestService;
    }

    @PutMapping("/put")
    public ResponseEntity<?> performPutRequest(@RequestBody PutRequestDto request) {
        try {
            Object response = putRequestService.sendPutRequest(request.getUrl(), request.getHeaders(), request.getBody(), request.getTimeout());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error while making PUT request: " + e.getMessage());
        }
    }
}