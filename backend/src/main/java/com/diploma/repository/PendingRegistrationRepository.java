package com.diploma.repository;

import com.diploma.model.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, UUID> {
    Optional<PendingRegistration> findByEmail(String email);
    boolean existsByEmail(String email);
}
