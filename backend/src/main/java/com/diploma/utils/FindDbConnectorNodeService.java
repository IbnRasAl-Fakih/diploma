package com.diploma.utils;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;


@Service
public class FindDbConnectorNodeService {

    private final SessionService service;

    public FindDbConnectorNodeService(SessionService service) {
        this.service = service;
    }

    public UUID findDbConnectorNodeId(Node node) throws Exception {
        if ("db_connector".equals(node.getType())) {
            return service.getByNodeId(node.getNodeId()).getSessionId();
        }

        if (node.getInputs() == null || node.getInputs().isEmpty()) {
            throw new Exception("Db connector node not found");
        }

        return findDbConnectorNodeId(node.getInputs().get(0));
    }
}