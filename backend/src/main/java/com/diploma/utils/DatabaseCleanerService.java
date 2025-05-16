package com.diploma.utils;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.diploma.model.Session;
import com.diploma.service.ResultService;

@Service
public class DatabaseCleanerService {
    
    private final SessionService sessionService;
    private final ResultService resultService;
    private final DatabaseConnectionPoolService dbService;

    public DatabaseCleanerService(SessionService sessionService, ResultService resultService, DatabaseConnectionPoolService dbService) {
        this.sessionService = sessionService;
        this.resultService = resultService;
        this.dbService = dbService;
    }

    public void clean(UUID workflowId) throws Exception {
        resultService.deleteByWorkflowId(workflowId);

        List<Session> sessions = sessionService.getByWorkflowId(workflowId);
        for(Session session : sessions) {
            dbService.removeConnection(session.getSessionId().toString());
        }

        sessionService.deleteByWorkflowId(workflowId);
    }
}