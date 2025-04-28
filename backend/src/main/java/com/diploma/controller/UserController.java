package com.diploma.controller;

import com.diploma.dto.UserResponseDto;
import com.diploma.model.User;
import com.diploma.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Tag(name = "Users", description = "Список всех пользователей || Получение пользователя по ID || Удаление пользователя по ID")
    public ResponseEntity<?> findAll() {
        try {
            List<User> users = service.findAll();
            List<UserResponseDto> response = users.stream()
                    .map(user -> new UserResponseDto(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail()
                    ))
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении списка пользователей: " + e.getMessage());
            return ResponseEntity.status(500).body("Ошибка при получении пользователей.");
        }
    }

    @GetMapping("/{id}")
    @Tag(name = "Users")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        try {
            User user = service.findById(id);
            if (user != null) {
                UserResponseDto response = new UserResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body("Пользователь не найден.");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении пользователя: " + e.getMessage());
            return ResponseEntity.status(500).body("Ошибка при получении пользователя.");
        }
    }

    @DeleteMapping("/{id}")
    @Tag(name = "Users")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("❌ Ошибка при удалении пользователя: " + e.getMessage());
            return ResponseEntity.status(500).body("Ошибка при удалении пользователя.");
        }
    }
}
