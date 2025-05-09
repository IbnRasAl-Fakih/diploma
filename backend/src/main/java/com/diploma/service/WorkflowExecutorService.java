package com.diploma.service;

import com.diploma.dto.ResultProcessorDto;
import com.diploma.dto.WorkflowExecutorRequestDto;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.utils.ResultProcessor;

import jakarta.annotation.PostConstruct;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WorkflowExecutorService {

    private final Map<String, NodeExecutor> executorRegistry = new HashMap<>();
    private final ResultProcessor processor;
    private final ApplicationContext context;

    public WorkflowExecutorService(ResultProcessor processor, ApplicationContext context) {
        this.processor = processor;
        this.context = context;
    }

    @PostConstruct
    public void registerExecutors() {
        Reflections reflections = new Reflections("com.diploma.service");

        Set<Class<? extends NodeExecutor>> classes = reflections.getSubTypesOf(NodeExecutor.class);

        for (Class<? extends NodeExecutor> clazz : classes) {
            NodeType annotation = clazz.getAnnotation(NodeType.class);
            if (annotation != null) {
                try {

                    NodeExecutor instance = context.getBean(clazz);
                    executorRegistry.put(annotation.value(), instance);
                    System.out.println("✅ Registered node type: " + annotation.value());
                } catch (Exception e) {
                    System.err.println("❌ Failed to register " + clazz.getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void executeWorkflow(WorkflowExecutorRequestDto dto) {
        UUID workflowId = dto.getWorkflowId();
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) dto.getNodes();

        for (Map<String, Object> node : nodes) {
            UUID nodeId = UUID.fromString(node.get("node_id").toString());
            String type = (String) node.get("type");

            @SuppressWarnings("unchecked")
            List<String> inputs = (List<String>) node.get("inputs");

            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) node.get("fields");

            NodeExecutor executor = executorRegistry.get(type);
            if (executor == null) {
                throw new IllegalArgumentException("❌ Unknown node type: " + type);
            }

            try {
                Object result = executor.execute(fields, inputs);
                processor.putToDatabase(new ResultProcessorDto(nodeId, workflowId, result));
            } catch (Exception e) {
                throw new RuntimeException("❌ Execution failed for node " + nodeId + ": " + e.getMessage(), e);
            }
        }
    }
}
