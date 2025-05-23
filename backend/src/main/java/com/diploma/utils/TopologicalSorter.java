package com.diploma.utils;

import java.util.*;

public class TopologicalSorter {

    public static List<Map<String, Object>> sort(List<Map<String, Object>> nodes) {
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Map<String, Object>> idToNode = new HashMap<>();

        for (Map<String, Object> node : nodes) {
            String id = (String) node.get("node_id");

            inDegree.put(id, 0);
            graph.put(id, new ArrayList<>());
            idToNode.put(id, node);
        }

        for (Map<String, Object> node : nodes) {
            String targetId = (String) node.get("node_id");
            List<String> inputs = (List<String>) node.get("inputs");

            if (inputs != null) {
                for (String inputId : inputs) {
                    if (!graph.containsKey(inputId)) {
                        throw new RuntimeException("❌ Неизвестный input: " + inputId);
                    }
                    graph.get(inputId).add(targetId);
                    inDegree.put(targetId, inDegree.get(targetId) + 1);
                }
            }
        }

        Queue<String> queue = new PriorityQueue<>();

        for (String id : inDegree.keySet()) {
            if (inDegree.get(id) == 0) {
                queue.add(id);
            }
        }

        List<Map<String, Object>> sorted = new ArrayList<>();

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            sorted.add(idToNode.get(currentId));
            
            for (String neighbor : graph.get(currentId)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        int expectedSize = nodes.size();
        if (sorted.size() != expectedSize) {
            throw new RuntimeException("Cycle in the graph or dependency violation (expected " + expectedSize + ", got " + sorted.size() + ")");
        }

        return sorted;
    }
}