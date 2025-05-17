package com.diploma.utils;

import org.springframework.stereotype.Service;

import com.diploma.model.Node;

@Service
public class FindNodeService {

    public Node findNode(Node node, String type) throws Exception {
        if (type.equals(node.getType())) {
            return node;
        }

        if (node.getInputs() == null || node.getInputs().isEmpty()) {
            throw new Exception("Node with type " + type + " not found");
        }

        return findNode(node.getInputs().get(0), type);
    }
}