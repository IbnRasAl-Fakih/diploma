package com.diploma.controller.HttpController;

import com.diploma.dto.HttpDto.PostRequestDto;
import com.diploma.service.HttpService.PostRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/http")
public class PostRequestController {

    private final PostRequestService postRequestService;

    public PostRequestController(PostRequestService postRequestService) {
        this.postRequestService = postRequestService;
    }

    @PostMapping("/post")
    public ResponseEntity<?> performPostRequest(@RequestBody PostRequestDto request) {
        try {
            Object response = postRequestService.sendPostRequest(request.getUrl(), request.getHeaders(), request.getBody(), request.getTimeout());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error while making POST request: " + e.getMessage());
        }
    }
}