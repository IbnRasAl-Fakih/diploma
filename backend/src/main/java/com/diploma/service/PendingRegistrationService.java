package com.diploma.service;

import com.diploma.model.PendingRegistration;
import com.diploma.repository.PendingRegistrationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PendingRegistrationService {

    private final PendingRegistrationRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PendingRegistrationService(PendingRegistrationRepository repository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void requestConfirmation(String username, String email, String rawPassword) throws Exception {
        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        Optional<PendingRegistration> existing = repository.findByEmail(email);

        if (existing.isPresent()) {
            PendingRegistration reg = existing.get();

            if (reg.getLastSentAt().isAfter(now.minusSeconds(60))) {
                throw new Exception("Слишком частая отправка. Подождите немного.");
            }

            reg.setUsername(username);
            reg.setPassword(passwordEncoder.encode(rawPassword));
            reg.setConfirmationCode(code);
            reg.setLastSentAt(now);
            reg.setExpiresAt(expiresAt);

            repository.save(reg);
        } else {
            PendingRegistration reg = new PendingRegistration();
            reg.setId(UUID.randomUUID());
            reg.setUsername(username);
            reg.setEmail(email);
            reg.setPassword(passwordEncoder.encode(rawPassword));
            reg.setConfirmationCode(code);
            reg.setCreatedAt(now);
            reg.setLastSentAt(now);
            reg.setExpiresAt(expiresAt);

            repository.save(reg);
        }

        System.out.println("📧 Отправлен код подтверждения " + code + " на email: " + email);
        
        emailService.sendConfirmationEmail(email, code);
    }

    private String generateCode() {
        return String.valueOf((int) (100000 + Math.random() * 900000));
    }

    public void requestEmailUpdate(UUID userId, String newEmail) throws Exception {
        if (userId == null || newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("userId и email не могут быть пустыми.");
        }
    
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);
        String code = generateCode();
    
        Optional<PendingRegistration> existing = repository.findByEmail(newEmail);
    
        if (existing.isPresent()) {
            PendingRegistration pending = existing.get();
    
            if (pending.getLastSentAt().isAfter(now.minusSeconds(60))) {
                throw new Exception("Слишком частая отправка. Подождите немного.");
            }
    
            pending.setConfirmationCode(code);
            pending.setLastSentAt(now);
            pending.setExpiresAt(expiresAt);
            pending.setUserId(userId);
            repository.save(pending);
        } else {
            PendingRegistration pending = new PendingRegistration();
            pending.setId(UUID.randomUUID());
            pending.setUsername("N/A");
            pending.setEmail(newEmail);
            pending.setPassword("N/A");
            pending.setConfirmationCode(code);
            pending.setCreatedAt(now);
            pending.setLastSentAt(now);
            pending.setExpiresAt(expiresAt);
            pending.setUserId(userId);
    
            repository.save(pending);
        }
    
        emailService.sendConfirmationEmail(newEmail, code);
    }    
}
