package com.diploma.service.IoToolsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;

import java.util.List;
import java.util.Map;

@Service
@NodeType("csv_writer")
public class CsvWriterService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(CsvWriterService.class);
    private final ResultService resultService;

    public CsvWriterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ CSV Writer: Missing input node.");
        }

        try {
            List<Map<String, Object>> data = resultService.getDataFromNode(node.getInputs().get(0).getNodeId());

            if (data == null) {
                throw new NodeExecutionException("❌ CSV Writer: Failed to get the result of the previous node.");
            }

            return data;

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("CSV Writer execution failed in method execute()", e);
            throw new NodeExecutionException("❌ CSV Writer execution failed.");
        }
    }
}