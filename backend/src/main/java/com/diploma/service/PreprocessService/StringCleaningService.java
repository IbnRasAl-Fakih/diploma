package com.diploma.service.PreprocessService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.*;

@Service
@NodeType("string_cleaning")
public class StringCleaningService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(StringCleaningService.class);
    private final ResultService resultService;

    public StringCleaningService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs() == null || node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ String Cleaner: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            List<Map<String, Object>> data;

            try {
                data = resultService.getDataFromNode(inputNodeId);
            } catch (Exception e) {
                throw new NodeExecutionException("❌ Failed to fetch input node data: " + e.getMessage(), e);
            }

            if (data == null) {
                throw new NodeExecutionException("❌ String Cleaner: Failed to get the result of the previous node.");
            }

            Map<String, List<String>> columnActions;
            List<String> globalActions;
            List<String> customCharsToRemove;

            try {
                columnActions = (Map<String, List<String>>) node.getFields().get("columnActions");
                globalActions = (List<String>) node.getFields().get("globalActions");
                customCharsToRemove = (List<String>) node.getFields().get("customCharsToRemove");
            } catch (ClassCastException e) {
                throw new NodeExecutionException("❌ String Cleaner: Invalid field types in node.");
            }

            List<Map<String, Object>> cleanedData;
            try {
                cleanedData = cleanStrings(data, columnActions, globalActions, customCharsToRemove);
            } catch (Exception e) {
                throw new NodeExecutionException("❌ String Cleaner: Error during cleaning process.");
            }

            return Map.of("result", cleanedData);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("❌ Unexpected error in String Cleaner node execution", e);
            throw new NodeExecutionException("❌ String Cleaner execution failed.");
        }
    }

    public List<Map<String, Object>> cleanStrings(List<Map<String, Object>> data, Map<String, List<String>> columnActions, List<String> globalActions, List<String> customCharsToRemove) throws Exception {
        try {
            if (data == null || data.isEmpty()) return data;

            List<Map<String, Object>> mutableData = new ArrayList<>();
            for (Map<String, Object> row : data) {
                Map<String, Object> copy = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    copy.put(entry.getKey(), entry.getValue());
                }
                mutableData.add(copy);
            }

            for (Map<String, Object> row : mutableData) {
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String column = entry.getKey();
                    Object rawValue = entry.getValue();

                    if (rawValue == null) continue;

                    String value = rawValue.toString();

                    List<String> actions = new ArrayList<>();
                    if (globalActions != null) actions.addAll(globalActions);
                    if (columnActions != null && columnActions.containsKey(column)) {
                        for (String action : columnActions.get(column)) {
                            if (!actions.contains(action)) {
                                actions.add(action);
                            }
                        }
                    }

                    for (String action : actions) {
                        switch (action) {
                            case "Trim" -> value = value.replaceAll("\\s{2,}", " ").trim();
                            case "Remove Non-Printable" -> value = value.replaceAll("\\p{C}", "");
                            case "Remove Custom Chars" -> {
                                if (customCharsToRemove != null) {
                                    for (String ch : customCharsToRemove) {
                                        if (ch != null && !ch.isEmpty()) {
                                            value = value.replace(ch, "");
                                        }
                                    }
                                }
                            }
                            default -> {}
                        }
                    }

                    row.put(column, value);
                }
            }
            return mutableData;

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ String Cleaner: Invalid input data structure.");

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("String Cleaner execution failed", e);
            throw new NodeExecutionException("❌ String Cleaner: Unknown error.");
        }
    }
}