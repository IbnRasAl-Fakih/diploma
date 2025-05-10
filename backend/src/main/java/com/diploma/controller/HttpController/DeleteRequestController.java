package com.diploma.controller.HttpController;

import com.diploma.dto.HttpDto.DeleteRequestDto;
import com.diploma.service.HttpService.DeleteRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/http")
public class DeleteRequestController {

    private final DeleteRequestService deleteRequestService;

    public DeleteRequestController(DeleteRequestService deleteRequestService) {
        this.deleteRequestService = deleteRequestService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> performDeleteRequest(@RequestBody DeleteRequestDto request) {
        try {
            Object response = deleteRequestService.sendDeleteRequest(request.getUrl(), request.getHeaders(), request.getBody(), request.getTimeout());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error while making DELETE request: " + e.getMessage());
        }
    }
}