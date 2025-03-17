package com.diploma.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class GlobalDataStorageService {

    private final Map<String, ArrayNode> globalResults = new ConcurrentHashMap<>();

    public void saveResult(String key, ArrayNode result) {
        globalResults.put(key, result);
    }

    public ArrayNode getResults(int offset, int limit, String key) {
        ArrayNode fullResults = globalResults.get(key);
        ArrayNode paginatedResults = fullResults.arrayNode();

        if (fullResults == null || offset >= fullResults.size()) {
            return paginatedResults;
        }

        int end = Math.min(offset + limit, fullResults.size());
        for (int i = offset; i < end; i++) {
            paginatedResults.add(fullResults.get(i));
        }

        return paginatedResults;
    }

    public boolean hasResult(String key) {
        return globalResults.containsKey(key);
    }

    public void removeResult(String key) {
        globalResults.remove(key);
    }

    public void clearAllResults() {
        globalResults.clear();
    }
}
