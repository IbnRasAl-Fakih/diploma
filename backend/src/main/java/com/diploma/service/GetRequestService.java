package com.diploma.service;

import com.diploma.dto.ResultProcessorDto;
import com.diploma.dto.ResultResponseDto;
import com.diploma.utils.ResultProcessor;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetRequestService {

    private final ResultProcessor processor;
    private final ObjectMapper mapper;

    public GetRequestService(ResultProcessor processor) {
        this.processor = processor;
        this.mapper = new ObjectMapper();
    }

    public String sendGetRequest(String url, Map<String, String> headers, Map<String, String> queryParams, int timeoutMillis, UUID workflowId, UUID nodeId) throws Exception {

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

        // Парсим ответ в JSON
        Object parsedJson = mapper.readValue(response.body(), new TypeReference<Object>() {});

        // Сохраняем
        ResultResponseDto saved = processor.putToDatabase(new ResultProcessorDto(nodeId, workflowId, parsedJson));

        return "✅ Данные успешно сохранены.";
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
