package com.example.interviewprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public OpenAIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(openaiApiUrl).build();
        this.objectMapper = objectMapper;
    }

    public Mono<String> generateQuestionReactive(String prompt) {
        // Define the request body
        String requestBody = "{\n" +
                "  \"model\": \"gpt-4o-mini\",\n" +
                "  \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}],\n" +
                "  \"temperature\": 0.7\n" +
                "}";

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError, // Handle error status directly with lambda
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody)))
                )
                .bodyToMono(String.class)
                .map(this::parseResponse);
    }

/*
    private ObjectNode createRequestBody(String prompt) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 150);
        return requestBody;
    }
*/

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
