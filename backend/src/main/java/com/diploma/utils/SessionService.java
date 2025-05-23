package com.diploma.utils;

import java.util.List;
import java.util.UUID;

import com.diploma.model.Session;
import com.diploma.repository.SessionRepository;

import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final SessionRepository repository;

    public SessionService (SessionRepository repository) {
        this.repository = repository;
    }
    
    public boolean doesSessionExist(UUID workflowId, String url) {
        return repository.existsByWorkflowIdAndUrl(workflowId, url);
    }

    public Session getByWorkflowIdAndUrl(UUID workflowId, String url) {
        return repository.findByWorkflowIdAndUrl(workflowId, url);
    }

    public List<Session> getByWorkflowId(UUID workflowid) {
        return repository.findByWorkflowId(workflowid);
    }

    public Session getByNodeId(UUID nodeId) {
        return repository.findByNodeId(nodeId);
    }

    public Session addSession(UUID workflowId, UUID nodeId, UUID sessionId, String url) {
        try {
            Session session = Session.builder()
                    .workflowId(workflowId)
                    .nodeId(nodeId)
                    .sessionId(sessionId)
                    .url(url)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();

            return repository.save(session);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось добавить сессию: " + e.getMessage(), e);
        }
    }

    public void deleteByWorkflowId(UUID workflowId) {
        repository.deleteByWorkflowId(workflowId);
    }
}