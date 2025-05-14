package com.diploma.utils;

import java.util.*;

public class TopologicalSorter {

    public static List<Map<String, Object>> sort(List<Map<String, Object>> nodes) {
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Map<String, Object>> idToNode = new HashMap<>();
        Set<String> readerIds = new HashSet<>();

        // 1. Инициализация графа, inDegree и определение reader-ноды
        for (Map<String, Object> node : nodes) {
            String id = (String) node.get("node_id");
            String type = (String) node.get("type");

            inDegree.put(id, 0);
            graph.put(id, new ArrayList<>());
            idToNode.put(id, node);

            if (type != null && type.toLowerCase().contains("reader")) {
                readerIds.add(id);
                System.out.println("📘 Reader node found: " + id);
            }
        }

        // 2. Построение графа и подсчёт входных степеней
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

        // 3. Инициализация PriorityQueue: non-reader ноды имеют приоритет
        Queue<String> queue = new PriorityQueue<>((a, b) -> {
            boolean aIsReader = readerIds.contains(a);
            boolean bIsReader = readerIds.contains(b);
            return Boolean.compare(aIsReader, bIsReader); // non-reader < reader
        });

        for (String id : inDegree.keySet()) {
            if (inDegree.get(id) == 0) {
                queue.add(id);
            }
        }

        // 4. Основной цикл топологической сортировки
        List<Map<String, Object>> sorted = new ArrayList<>();

        while (!queue.isEmpty()) {
            String currentId = queue.poll();

            if (!readerIds.contains(currentId)) {
                sorted.add(idToNode.get(currentId));
            }

            for (String neighbor : graph.get(currentId)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor); // порядок соблюдается автоматически
                }
            }
        }

        // 5. Проверка на циклы
        int expectedSize = nodes.size() - readerIds.size();
        if (sorted.size() != expectedSize) {
            throw new RuntimeException("❌ Цикл в графе или нарушена зависимость (ожидалось "
                    + expectedSize + ", получено " + sorted.size() + ")");
        }

        return sorted;
    }
}