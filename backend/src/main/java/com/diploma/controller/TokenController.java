package com.diploma.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.diploma.utils.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final JwtService jwtService;

    public TokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/decode")
    @Tag(name = "Token Controller", description = "Контроллер для получения данных из токена")
    public ResponseEntity<?> decodeToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");

            String email = jwtService.getEmailFromToken(token);
            var userId = jwtService.getUserIdFromToken(token);

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", email
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Неверный токен"));
        }
    }
}