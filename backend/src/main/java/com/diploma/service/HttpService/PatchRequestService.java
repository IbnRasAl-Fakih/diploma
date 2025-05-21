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

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@NodeType("patch_request")
public class PatchRequestService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(PatchRequestService.class);
    private final ResultService resultService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PatchRequestService (ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {

        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ PATCH Request: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();
            List<Map<String, Object>> body = resultService.getDataFromNode(inputNodeId);

            if (body == null) {
                throw new NodeExecutionException("❌ PATCH Request: Failed to get the result of the previous node.");
            }

            String url = (String) node.getFields().get("url");

            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) node.getFields().getOrDefault("headers", Map.of());

            Object timeoutObj = node.getFields().get("timeout");
            int timeoutMillis;

            if (timeoutObj instanceof Integer) {
                timeoutMillis = (Integer) timeoutObj;
            } else if (timeoutObj instanceof String strVal && strVal.matches("\\d+")) {
                timeoutMillis = Integer.parseInt(strVal);
            } else {
                throw new NodeExecutionException("❌ PATCH Request: 'timeout' must be an integer value.");
            }

            if (url == null || url == "" || headers == null) {
                throw new NodeExecutionException("❌ PATCH Request: Missing required fields.");
            }

            return sendPatchRequest(url, headers, body, timeoutMillis);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("PATCH Request execution failed in method execute()", e);
            throw new NodeExecutionException("❌ PATCH Request execution failed.");
        }
    }

    public Object sendPatchRequest(String url, Map<String, String> headers, List<Map<String, Object>> body, int timeoutMillis) throws Exception {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMillis))
                .build();

            String requestBody = mapper.writeValueAsString(body);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));

            if (headers != null) {
                headers.forEach(requestBuilder::header);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new NodeExecutionException("❌ PATCH Request failed: HTTP " + statusCode + " — " + response.body());
            }

            return mapper.readValue(response.body(), Object.class);

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (UnknownHostException e) {
            throw new NodeExecutionException("❌ PATCH Request: Unknown host - " + e.getMessage());

        } catch (ConnectException e) {
            throw new NodeExecutionException("❌ PATCH Request: Connection refused - " + e.getMessage());

        } catch (HttpTimeoutException e) {
            throw new NodeExecutionException("❌ PATCH Request: Timeout exceeded.");
            
        } catch (Exception e) {
            log.error("PATCH Request execution failed", e);
            throw new NodeExecutionException("❌ PATCH Request: Unknown error.");
        }
    }
}