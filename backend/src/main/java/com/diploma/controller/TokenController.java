package com.diploma.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.diploma.service.UserService;
import com.diploma.utils.JwtService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final JwtService jwtService;
    private final UserService userService;

    public TokenController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @GetMapping("/decode")
    @Tag(name = "Token Controller", description = "Контроллер для получения данных из токена")
    public ResponseEntity<?> decodeToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");

            String email = jwtService.getEmailFromToken(token);
            UUID userId = jwtService.getUserIdFromToken(token);
            String username = userService.findById(userId).getUsername();

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", email,
                "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Неверный токен"));
        }
    }
}