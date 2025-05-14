package com.diploma.repository;

import com.diploma.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResultRepository extends JpaRepository<Result, UUID> {
    List<Result> findByWorkflowId(UUID workflowId);
}