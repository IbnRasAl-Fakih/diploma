package com.diploma.service.HttpService;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("delete_request")
public class DeleteRequestService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DeleteRequestService.class);
    private final ResultService resultService;
    private final ObjectMapper mapper = new ObjectMapper();

    public DeleteRequestService (ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {

        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ DELETE Request: Missing input nodes.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            List<Map<String, Object>> body = resultService.getDataFromNode(inputNodeId);

            if (body == null) {
                throw new NodeExecutionException("❌ DELETE Request: Failed to get the result of the previous node.");
            }

            String url = (String) node.getFields().get("url");

            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) node.getFields().getOrDefault("headers", Map.of());

            int timeoutMillis = (Integer) node.getFields().get("timeout");

            if (timeoutMillis < 3000) {
                timeoutMillis = 3000;
            }

            if (url == null || url == "" || headers == null) {
                throw new NodeExecutionException("❌ DELETE Request: Missing required fields.");
            }

            return sendDeleteRequest(url, headers, body, timeoutMillis);
        } catch (Exception e) {
            log.error("DELETE Request execution failed in method execute()", e);
            throw new NodeExecutionException("❌ DELETE Request execution failed: ", e);
        }
    }

    public Object sendDeleteRequest(String url, Map<String, String> headers, List<Map<String, Object>> body, int timeoutMillis) throws Exception {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMillis))
                .build();

            String requestBody = mapper.writeValueAsString(body);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));

            if (headers != null) {
                headers.forEach(requestBuilder::header);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return mapper.readValue(response.body(), Object.class);
        } catch (Exception e) {
            log.error("DELETE Request execution failed", e);
            throw new NodeExecutionException("❌ DELETE Request: ", e);
        }
    }
}