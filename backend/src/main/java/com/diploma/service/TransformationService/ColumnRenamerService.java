package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.ColumnRenamerRequest;
import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NodeType("column_renamer")
public class ColumnRenamerService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(ColumnRenamerService.class);
    private final ResultService resultService;

    public ColumnRenamerService(ResultService resultService) {
        this.resultService = resultService;
    }
    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Column Renamer: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Column Renamer: Failed to get the result of the previous node.");
            }

            ColumnRenamerRequest request = new ColumnRenamerRequest();
            request.setData(data);
            request.setRenameMap((Map<String, String>) node.getFields().get("renameMap"));

            List<Map<String, Object>> result = renameColumns(request);

            return Map.of("result", result);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Column Renamer execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Column Renamer execution failed.");
        }
    }

    public List<Map<String, Object>> renameColumns(ColumnRenamerRequest req) throws Exception {
        try {

            if (req.getRenameMap() == null) {
                throw new NodeExecutionException("❌ Column Renamer: Required fields are null.");
            }

            if (req.getRenameMap().containsValue(null) || req.getRenameMap().containsValue("")) {
                throw new NodeExecutionException("❌ Column Renamer: Rename map contains null or empty target column names.");
            }

            List<Map<String, Object>> renamed = new ArrayList<>();

            for (Map<String, Object> row : req.getData()) {
                Map<String, Object> newRow = new LinkedHashMap<>();

                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String originalKey = entry.getKey();
                    String renamedKey = req.getRenameMap().getOrDefault(originalKey, originalKey);

                    if (newRow.containsKey(renamedKey)) {
                        throw new NodeExecutionException("❌ Column Renamer: Duplicate column name after renaming: " + renamedKey);
                    }

                    newRow.put(renamedKey, entry.getValue());
                }

                renamed.add(newRow);
            }

            return renamed;

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Column Renamer: Invalid row structure.");

        } catch (Exception e) {
            log.error("Column Renamer execution failed", e);
            throw new NodeExecutionException("❌ Column Renamer: Unknown error.");
        }
    }
}