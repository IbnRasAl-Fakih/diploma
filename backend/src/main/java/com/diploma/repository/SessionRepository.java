package com.diploma.repository;

import com.diploma.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByWorkflowId(UUID workflowId);

    Session findByNodeId(UUID nodeId);

    Session findByWorkflowIdAndUrl(UUID workflowId, String url);

    boolean existsByWorkflowIdAndUrl(UUID workflowId, String url);

    @Transactional
    void deleteByWorkflowId(UUID workflowId);
}