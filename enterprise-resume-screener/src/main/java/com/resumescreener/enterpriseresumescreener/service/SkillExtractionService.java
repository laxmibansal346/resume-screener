package com.resumescreener.enterpriseresumescreener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class SkillExtractionService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SkillExtractionService(@Value("${groq.api.key}") String groqApiKey) {
        this.chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(groqApiKey)
                .modelName("openai/gpt-oss-120b")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(0)
                .build();
    }

    public List<String> extractSkills(String jobDescription) throws Exception {
        String prompt = buildPrompt(jobDescription);

        String rawResponse = chatModel.chat(prompt);
        String cleanJson = cleanJsonResponse(rawResponse);

        return objectMapper.readValue(cleanJson, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    }

    private String buildPrompt(String jobDescription) {
        return """
                You are an expert recruiter across all industries (technical, management, healthcare, pharmacy, finance, marketing, and others).

                Read the following JOB DESCRIPTION and extract ONLY the important skills, qualifications, tools, and domain knowledge that are EXPLICITLY mentioned in the text. This may include technical skills, software tools, certifications, domain expertise, regulatory knowledge, or professional competencies — whatever is relevant to this specific role.

                IMPORTANT RULES:
                - Do NOT guess, assume, or add skills that are not directly stated in the text.
                - If the text does not look like a real job description (too short, unclear, random text, or missing actual requirements), return an empty JSON array: []
                - Only include skills/qualifications you can directly point to in the given text.

                JOB DESCRIPTION:
                %s

                Respond ONLY with a valid JSON array of strings. No extra text, no explanation, no markdown code fences.
                Example format:
                ["Java", "Spring Boot", "Stakeholder Management", "GMP Compliance"]
                """.formatted(jobDescription);
    }

    private String cleanJsonResponse(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned
                    .replaceAll("^```json", "")
                    .replaceAll("^```", "")
                    .replaceAll("```$", "")
                    .trim();
        }
        return cleaned;
    }
}