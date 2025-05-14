package com.diploma.utils;

import com.diploma.model.Node;

import java.util.*;

public class NodeMapper {

    public static List<Node> mapToNodeList(List<Map<String, Object>> sortedNodes) {
        Map<String, Node> idToNodeObject = new HashMap<>();
        List<Node> nodeList = new ArrayList<>();

        // 1. Сначала создаём все объекты Node с пустым inputs
        for (Map<String, Object> nodeMap : sortedNodes) {
            Node node = mapToNode(nodeMap);
            idToNodeObject.put(node.getNodeId().toString(), node);
            nodeList.add(node);
        }

        // 2. Потом заполняем поле inputs реальными Node (связями)
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

        Map<String, Object> fields = (Map<String, Object>) nodeMap.get("fields");

        Node node = new Node();
        node.setNodeId(nodeId);
        node.setType(type);
        node.setFields(fields);
        node.setInputs(new ArrayList<>()); // пока пустой, заполнится позже

        return node;
    }
}