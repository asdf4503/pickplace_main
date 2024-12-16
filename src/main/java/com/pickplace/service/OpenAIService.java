package com.pickplace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askQuestion(String question) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", question);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(message));
        requestBody.put("max_tokens", 100);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("choices")) {
            Map<String, Object> firstChoice = (Map<String, Object>) ((List<Object>) responseBody.get("choices")).get(0);
            Map<String, Object> messageContent = (Map<String, Object>) firstChoice.get("message");
            return messageContent.get("content").toString().trim();
        }

        return "No response from OpenAI";
    }
}
