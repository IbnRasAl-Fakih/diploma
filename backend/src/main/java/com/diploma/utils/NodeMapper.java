package com.diploma.utils;

import com.diploma.model.Node;

import java.util.*;

public class NodeMapper {

    public static List<Node> mapToNodeList(List<Map<String, Object>> sortedNodes) {
        Map<String, Node> idToNodeObject = new HashMap<>();
        List<Node> nodeList = new ArrayList<>();

        for (Map<String, Object> nodeMap : sortedNodes) {
            Node node = mapToNode(nodeMap);
            idToNodeObject.put(node.getNodeId().toString(), node);
            nodeList.add(node);
        }

        for (int i = 0; i < sortedNodes.size(); i++) {
            Node node = nodeList.get(i);
            Map<String, Object> nodeMap = sortedNodes.get(i);

            List<Node> updatedInputs = new ArrayList<>();
            List<String> inputsIds = (List<String>) nodeMap.get("inputs");

            if (inputsIds != null) {
                for (String inputId : inputsIds) {
                    Node inputNode = idToNodeObject.get(inputId);
                    if (inputNode != null) {
                        updatedInputs.add(inputNode);
                    }
                }
            }

            node.setInputs(updatedInputs);
        }

        return nodeList;
    }

    private static Node mapToNode(Map<String, Object> nodeMap) {
        UUID nodeId = UUID.fromString((String) nodeMap.get("node_id"));
        String type = (String) nodeMap.get("type");

        if (type == "excel_reader" || type == "csv_reader") {
            return new Node(nodeId, type, new ArrayList<>(), null);
        }

        Map<String, Object> fields = (Map<String, Object>) nodeMap.get("fields");

        return new Node(nodeId, type, new ArrayList<>(), fields);
    }
}