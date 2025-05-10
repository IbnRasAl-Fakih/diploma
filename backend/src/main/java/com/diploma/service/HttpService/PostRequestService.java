package com.diploma.service.HttpService;

import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@NodeType("post_request")
public class PostRequestService implements NodeExecutor {

    private final ResultService resultService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PostRequestService (ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Map<String, Object> fields, List<String> inputs) {
        try {
            if (inputs.isEmpty()) {
                throw new IllegalArgumentException("POST Request требует хотя бы один input (nodeId)");
            }

            UUID inputNodeId = UUID.fromString(inputs.get(0));
            List<Map<String, Object>> body = resultService.getDataFromNode(inputNodeId);

            String url = (String) fields.get("url");

            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) fields.getOrDefault("headers", Map.of());

            int timeoutMillis = (Integer) fields.get("timeout");

            return sendPostRequest(url, headers, body, timeoutMillis);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public Object sendPostRequest(String url, Map<String, String> headers, List<Map<String, Object>> body, int timeoutMillis) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMillis)).build();

        String requestBody = mapper.writeValueAsString(body);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMillis))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), Object.class);
    }
}