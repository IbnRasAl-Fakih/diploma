package com.diploma.service.PreprocessService;

import org.springframework.stereotype.Service;

import com.diploma.dto.PreprocessDto.StringCleaningRequest;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.*;

@Service
@NodeType("string_cleaning")
public class StringCleaningService implements NodeExecutor {

    private final ResultService resultService;

    public StringCleaningService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("Missing Data Processing требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = node.getInputs().get(0).getNodeId();

        List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

        StringCleaningRequest request = new StringCleaningRequest();
        request.setData(data);
        request.setColumnActions((Map<String, List<String>>) node.getFields().get("columnActions"));
        request.setGlobalActions((List<String>) node.getFields().get("globalActions"));
        request.setCustomCharsToRemove((List<String>) node.getFields().get("customCharsToRemove"));

        List<Map<String, Object>> filtred = cleanStrings(request);

        return Map.of("Cleaned", filtred);
    }

    public List<Map<String, Object>> cleanStrings(StringCleaningRequest request) {
        List<Map<String, Object>> data = request.getData();
        Map<String, List<String>> columnActions = request.getColumnActions();
        List<String> globalActions = request.getGlobalActions();
        List<String> customCharsToRemove = request.getCustomCharsToRemove();

        if (data == null || data.isEmpty())
            return data;

        for (Map<String, Object> row : data) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String column = entry.getKey();
                Object rawValue = entry.getValue();

                if (rawValue == null)
                    continue;

                String value = rawValue.toString();
                Set<String> actions = new HashSet<>();
                if (globalActions != null)
                    actions.addAll(globalActions);
                if (columnActions != null && columnActions.containsKey(column))
                    actions.addAll(columnActions.get(column));

                for (String action : actions) {
                    switch (action) {
                        case "Trim":
                            value = value.replaceAll("\\s{2,}", " ").trim();
                            break;
                        case "Remove Non-Printable":
                            value = value.replaceAll("\\p{C}", "");
                            break;
                        case "Remove Custom Chars":
                            if (customCharsToRemove != null) {
                                for (String ch : customCharsToRemove) {
                                    value = value.replace(ch, "");
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }

                row.put(column, value);
            }
        }

        return data;
    }
}