package com.diploma.controller.HttpController;

import com.diploma.dto.HttpDto.PatchRequestDto;
import com.diploma.service.HttpService.PatchRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/http")
public class PatchRequestController {

    private final PatchRequestService patchRequestService;

    public PatchRequestController(PatchRequestService patchRequestService) {
        this.patchRequestService = patchRequestService;
    }

    @PatchMapping("/patch")
    public ResponseEntity<?> performPatchRequest(@RequestBody PatchRequestDto request) {
        try {
            Object response = patchRequestService.sendPatchRequest(request.getUrl(), request.getHeaders(), request.getBody(), request.getTimeout());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error while making PATCH request: " + e.getMessage());
        }
    }
}