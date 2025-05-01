package com.diploma.service;

import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@NodeType("get_request")
public class GetRequestService implements NodeExecutor{

    private final ObjectMapper mapper;

    public GetRequestService() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public Object execute(Map<String, Object> fields, List<String> inputs) {
        try {
            String url = (String) fields.get("url");

            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) fields.getOrDefault("headers", Map.of());

            @SuppressWarnings("unchecked")
            Map<String, String> queryParams = (Map<String, String>) fields.getOrDefault("queryParams", Map.of());

            int timeoutMillis = (Integer) fields.get("timeout");

            System.out.println("\nurl: " + url + ", headers: " + headers.toString() + "timeout: " + timeoutMillis + "\n"); // delete

            return sendGetRequest(url, headers, queryParams, timeoutMillis);
        } catch (Exception e) {
            System.err.println("❌ Error in execute(): " + e.getMessage());
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    public Object sendGetRequest(String url, Map<String, String> headers, Map<String, String> queryParams, int timeoutMillis) throws Exception {
        String fullUrl = url + "?" + getQueryParamString(queryParams);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMillis))
                .build();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofMillis(timeoutMillis))
                .GET();

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), new TypeReference<Object>() {});
    }

    private String getQueryParamString(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        return queryParams.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}