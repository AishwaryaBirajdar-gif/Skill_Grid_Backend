package com.SkillExchange.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.SkillExchange.DTO.SkillResponse;

import java.util.List;
import java.util.Map;

@Service
public class SkillAIService {

    @Value("${groq.api.url}")
    private String url;

    @Value("${groq.api.key}")
    private String key;

    @Value("${groq.model}")
    private String model;

    private final WebClient webClient = WebClient.create();

    public SkillResponse analyzeSkill(String skill) {

        String response = webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of(
                                        "role", "system",
                                        "content", "Return ONLY valid JSON without any explanation.\n" +
                                        		"{\"skill\":\"string\",\"level\":\"Beginner\"|\"Intermediate\"|\"Expert\",\"description\":\"string\"}\n" +
                                        		"level MUST be only ONE value, not multiple, not pipe-separated."
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", skill
                                )
                        )
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String content = extractContent(response);

        JSONObject json = new JSONObject(content);

        return new SkillResponse(
                json.optString("skill", skill),
                json.optString("level"),
                json.optString("description")
        );
    }

    private String extractContent(String response) {
        JSONObject obj = new JSONObject(response);
        return obj.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}