package com.diploma.controller;

import com.diploma.dto.*;
import com.diploma.model.PendingRegistration;
import com.diploma.model.User;
import com.diploma.repository.PendingRegistrationRepository;
import com.diploma.repository.UserRepository;
import com.diploma.service.JwtService;
import com.diploma.service.PendingRegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PendingRegistrationService pendingService;
    private final PendingRegistrationRepository pendingRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            PendingRegistrationService pendingService,
            PendingRegistrationRepository pendingRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.pendingService = pendingService;
        this.pendingRepo = pendingRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/request-confirmation")
    @Tag(name = "Sign In |Sign Up")
    @Operation(summary = "Регистрация нового пользователя: отправка кода на почту")
    public ResponseEntity<?> requestConfirmation(@RequestBody ConfirmationRequestDto dto) {
        try {
            pendingService.requestConfirmation(dto.getUsername(), dto.getEmail(), dto.getPassword());
            return ResponseEntity.ok("Код подтверждения отправлен на email.");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отправке кода: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm")
    @Tag(name = "Sign In |Sign Up")
    @Operation(summary = "Регистрация нового пользователя: подтверждение кода")
    public ResponseEntity<?> confirm(@RequestBody EmailCodeDto dto) {
        try {
            PendingRegistration pending = pendingRepo.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Нет запроса на регистрацию с таким email."));

            if (pending.getUserId() != null) {
                return ResponseEntity.badRequest().body("Это запрос на обновление email, а не регистрация.");
            }

            if (!pending.getConfirmationCode().equals(dto.getConfirmationCode())) {
                return ResponseEntity.badRequest().body("Неверный код подтверждения.");
            }

            if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Код истёк. Запросите новый.");
            }

            if (userRepo.existsByEmail(dto.getEmail())) {
                return ResponseEntity.badRequest().body("Пользователь с таким email уже существует.");
            }

            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername(pending.getUsername());
            user.setEmail(pending.getEmail());
            user.setPassword(pending.getPassword());
            userRepo.save(user);

            pendingRepo.delete(pending);
            return ResponseEntity.ok("Почта подтверждена, пользователь зарегистрирован.");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при подтверждении регистрации: " + e.getMessage());
            return ResponseEntity.status(500).body("Ошибка при подтверждении.");
        }
    }

    @PostMapping("/request-email-update")
    @Tag(name = "Sign In |Sign Up")
    @Operation(summary = "Обновление данных пользователя: отправка кода на почту")
    public ResponseEntity<?> requestEmailUpdate(@RequestBody EmailUpdateRequestDto dto) {
        try {
            if (!userRepo.existsById(dto.getUserId())) {
                return ResponseEntity.status(404).body("Пользователь не найден.");
            }

            pendingService.requestEmailUpdate(dto.getUserId(), dto.getNewEmail());
            return ResponseEntity.ok("Код подтверждения отправлен на новый email.");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при запросе смены email: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm-email-update")
    @Tag(name = "Sign In |Sign Up")
    @Operation(summary = "Обновление данных пользователя: подтверждение кода")
    public ResponseEntity<?> confirmEmailUpdate(@RequestBody EmailCodeDto dto) {
        try {
            PendingRegistration pending = pendingRepo.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Нет активного запроса на смену email."));

            if (pending.getUserId() == null) {
                return ResponseEntity.badRequest().body("Это запрос регистрации, а не смены email.");
            }

            if (!pending.getConfirmationCode().equals(dto.getConfirmationCode())) {
                return ResponseEntity.badRequest().body("Неверный код подтверждения.");
            }

            if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Код истёк. Запросите новый.");
            }

            if (userRepo.existsByEmail(dto.getEmail())) {
                return ResponseEntity.badRequest().body("Email уже занят другим пользователем.");
            }

            User user = userRepo.findById(pending.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден."));

            user.setEmail(dto.getEmail());
            userRepo.save(user);

            pendingRepo.delete(pending);
            return ResponseEntity.ok("Email успешно обновлён.");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при подтверждении смены email: " + e.getMessage());
            return ResponseEntity.status(500).body("Ошибка при подтверждении смены email.");
        }
    }

    @PostMapping("/login")
    @Tag(name = "Sign In |Sign Up")
    @Operation(summary = "Логин через email-password")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        try {
            User user = userRepo.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден."));

            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Неверный пароль.");
            }

            String token = jwtService.generateToken(user.getId(), user.getEmail());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (Exception e) {
            System.err.println("❌ Ошибка входа: " + e.getMessage());
            return ResponseEntity.status(500).body("Ошибка входа.");
        }
    }
}
