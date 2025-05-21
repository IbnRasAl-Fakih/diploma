package com.diploma.service;

import com.diploma.dto.ResultProcessorDto;
import com.diploma.dto.WorkflowExecutorRequestDto;
import com.diploma.exception.NodeExecutionException;
import com.diploma.utils.DatabaseCleanerService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeMapper;
import com.diploma.utils.NodeType;
import com.diploma.utils.ResultProcessor;
import com.diploma.utils.SessionService;
import com.diploma.utils.TopologicalSorter;
import com.diploma.model.Node;
import com.diploma.model.Session;

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
    private final SessionService sessionService;
    private final DatabaseCleanerService dbCleanerService;

    public WorkflowExecutorService(ResultProcessor processor, ApplicationContext context, SessionService sessionService, DatabaseCleanerService dbCleanerService) {
        this.processor = processor;
        this.context = context;
        this.sessionService = sessionService;
        this.dbCleanerService = dbCleanerService;
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
                    System.out.println("Registered node type: " + annotation.value());
                } catch (Exception e) {
                    System.err.println("Failed to register " + clazz.getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void executeWorkflow(WorkflowExecutorRequestDto dto) throws Exception {
        UUID workflowId = dto.getWorkflowId();
        dbCleanerService.clean(workflowId);
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) dto.getNodes();
        
        
        for (Map<String, Object> node : nodes) {
            System.out.println("Node:");
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
        }

        List<Map<String, Object>> sortedNodes = TopologicalSorter.sort(nodes);

        List<Node> sortedMappedNodes = NodeMapper.mapToNodeList(sortedNodes);

        for (Node node : sortedMappedNodes) {

            NodeExecutor executor = executorRegistry.get(node.getType());
            if (executor == null) {
                throw new IllegalArgumentException("❌ Unknown node type: " + node.getType());
            }

            if ("excel_reader".equals(node.getType()) || "csv_reader".equals(node.getType())) {
                continue;
            }

            try {
                if ("db_connector".equals(node.getType())) {
                    String url = (String) node.getFields().get("url");

                    if (sessionService.doesSessionExist(workflowId, url)) {
                        Session session = sessionService.getByWorkflowIdAndUrl(workflowId, url);
                        processor.putToDatabase(new ResultProcessorDto(node.getNodeId(), node.getType(), workflowId, Map.of("sessionId", session.getSessionId())));
                        sessionService.addSession(workflowId, node.getNodeId(), session.getSessionId(), url);
                    } else {
                        Object result = executor.execute(node);
                        processor.putToDatabase(new ResultProcessorDto(node.getNodeId(), node.getType(), workflowId, result));
                        sessionService.addSession(workflowId, node.getNodeId(), UUID.fromString((String) ((Map<?, ?>) result).get("sessionId")), url);
                    }
                } else {
                    Object result = executor.execute(node);
                    processor.putToDatabase(new ResultProcessorDto(node.getNodeId(), node.getType(), workflowId, result));
                }
            } catch (Exception e) {
                if (e instanceof NodeExecutionException) {
                    throw (NodeExecutionException) e;
                }
                throw new NodeExecutionException("❌ Execution failed for node " + node.getNodeId(), e);
            }
        }
    }
}