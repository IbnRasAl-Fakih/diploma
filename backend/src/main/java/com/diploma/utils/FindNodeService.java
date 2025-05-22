package com.diploma.utils;

import com.diploma.model.Node;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindNodeService {

    public record FoundNode(Node node, int inputIndex) {}

    public FoundNode findNode(Node node, String type) {
        if (node == null || type == null) {
            return null;
        }

        if (type.equals(node.getType())) {
            return new FoundNode(node, -1);
        }

        List<Node> inputs = node.getInputs();
        if (inputs == null || inputs.isEmpty()) {
            return null;
        }

        for (int i = 0; i < inputs.size(); i++) {
            Node inputNode = inputs.get(i);
            FoundNode found = findNode(inputNode, type);
            if (found != null) {
                return new FoundNode(found.node(), i);
            }
        }

        return null;
    }
}