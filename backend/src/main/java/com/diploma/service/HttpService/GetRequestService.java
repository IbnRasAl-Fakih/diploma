package com.diploma.service.HttpService;

import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import com.diploma.exception.NodeExecutionException;
import com.diploma.model.Node;

@Service
@NodeType("get_request")
public class GetRequestService implements NodeExecutor{

    private static final Logger log = LoggerFactory.getLogger(GetRequestService.class);
    private final ObjectMapper mapper;

    public GetRequestService() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public Object execute(Node node) throws Exception {
        try {
            String url = (String) node.getFields().get("url");

            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) node.getFields().getOrDefault("headers", Map.of());

            @SuppressWarnings("unchecked")
            Map<String, String> queryParams = (Map<String, String>) node.getFields().getOrDefault("queryParams", Map.of());

            int timeoutMillis = (Integer) node.getFields().get("timeout");

            if (url == null || url == "" || headers == null || queryParams == null) {
                throw new NodeExecutionException("❌ GET Request: Missing required fields.");
            }

            return sendGetRequest(url, headers, queryParams, timeoutMillis);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("GET Request execution failed in method execute()", e);
            throw new NodeExecutionException("❌ GET Request execution failed");
        }
    }

    public Object sendGetRequest(String url, Map<String, String> headers, Map<String, String> queryParams, int timeoutMillis) throws Exception {
        try {
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

            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new NodeExecutionException("❌ GET Request failed: HTTP " + statusCode + " — " + response.body());
            }

            return mapper.readValue(response.body(), new TypeReference<Object>() {});

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (UnknownHostException e) {
            throw new NodeExecutionException("❌ GET Request: Unknown host - " + e.getMessage(), e);

        } catch (ConnectException e) {
            throw new NodeExecutionException("❌ GET Request: Connection refused - " + e.getMessage(), e);

        } catch (HttpTimeoutException e) {
            throw new NodeExecutionException("❌ GET Request: Timeout exceeded", e);
            
        } catch (Exception e) {
            log.error("GET Request execution failed", e);
            throw new NodeExecutionException("❌ GET Request: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
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