package com.resumematcher.matching_service.service;

import com.resumematcher.matching_service.config.GeminiConfig;
import com.resumematcher.matching_service.dto.GeminiRequest;
import com.resumematcher.matching_service.dto.GeminiResponse;
import com.resumematcher.matching_service.dto.MatchAnalysis;
import com.resumematcher.matching_service.exception.LlmResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class GeminiClient {

    private final RestClient restClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MatchAnalysis analyzeMatch(String resumeText, String jobText) {
        String prompt = PromptBuilder.buildMatchPrompt(resumeText, jobText);
        GeminiRequest request = GeminiRequest.of(prompt);

        GeminiResponse response = restClient.post()
                .uri(geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);

        if(response==null) throw new RuntimeException("Failed to get response from Gemini API");

        String rawText = response.extractText();
        String cleanedJson = stripMarkdownFences(rawText);

        try {
            return objectMapper.readValue(cleanedJson, MatchAnalysis.class);
        } catch (Exception e) {
            throw new LlmResponseException("Failed to parse LLM response as JSON: " + rawText);
        }

    }

    private String stripMarkdownFences(String text) {
        return text
                .trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("```\\s*$", "")
                .trim();
    }
}
