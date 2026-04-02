package com.example.ragsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Direct REST client to interface with AnythingLLM specific features
 * if you choose to bypass Spring AI's standardized ChatModel for
 * workspace-specific tasks.
 */
@Service
public class AnythingLLMClient {

    private final RestTemplate restTemplate;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public AnythingLLMClient() {
        this.restTemplate = new RestTemplate();
    }

    public String sendWorkspaceChat(String slug, String message) {
        String url = baseUrl.replace("/v1", "") + "/workspace/" + slug + "/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, String> body = Map.of("message", message, "mode", "chat");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        return response.getBody();
    }
}
