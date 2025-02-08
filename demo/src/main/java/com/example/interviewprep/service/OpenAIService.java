package com.example.interviewprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAIService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Mono<String> generateQuestionReactive(String prompt) {
        ObjectNode requestBody = createRequestBody(prompt);

        return webClient.post()
                .uri(openaiApiUrl)
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody.toString())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError, // Handle error status directly with lambda
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody)))
                )
                .bodyToMono(String.class)
                .map(this::parseResponse);
    }

    private ObjectNode createRequestBody(String prompt) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 150);
        return requestBody;
    }

    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").path(0).path("text").asText("No text generated.").trim();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response", e);
        }
    }

    public Mono<String> evaluateAnswerReactive(String userAnswer, String questionPrompt) {
        String prompt = String.format("Evaluate the following answer for the question:\n%s\n\nAnswer: %s\n\nProvide detailed feedback and suggestions for improvement.",
                questionPrompt, userAnswer);
        return generateQuestionReactive(prompt);
    }
}
