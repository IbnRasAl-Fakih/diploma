package com.diploma.repository;

import com.diploma.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByWorkflowId(UUID workflowId);

    Optional<Session> findByNodeId(UUID nodeId);

    Session findByWorkflowIdAndUrl(UUID workflowId, String url);

    boolean existsByWorkflowIdAndUrl(UUID workflowId, String url);

    void deleteByWorkflowId(UUID workflowId);
}