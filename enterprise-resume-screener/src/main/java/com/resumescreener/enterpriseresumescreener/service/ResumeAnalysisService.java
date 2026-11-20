package com.resumescreener.enterpriseresumescreener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumescreener.enterpriseresumescreener.dto.AnalysisResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ResumeAnalysisService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeAnalysisService(@Value("${groq.api.key}") String groqApiKey) {
        this.chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(groqApiKey)
                .modelName("openai/gpt-oss-120b")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(0)
                .build();
    }

    public AnalysisResult analyze(String resumeText, String jobDescription) throws Exception {
        String prompt = buildPrompt(resumeText, jobDescription);

        String rawResponse = chatModel.chat(prompt);

        String cleanJson = cleanJsonResponse(rawResponse);

        return objectMapper.readValue(cleanJson, AnalysisResult.class);
    }

    private String buildPrompt(String resumeText, String jobDescription) {
        return """
                You are an expert HR recruiter and resume screener.

                Compare the following RESUME and JOB DESCRIPTION carefully.

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Respond ONLY in valid JSON format. No extra text, no explanation, no markdown code fences.
                Follow this exact structure:

                {
                  "matchPercentage": <integer between 0 and 100>,
                  "matchedKeywords": [<important skills/keywords present in both resume and job description>],
                  "missingKeywords": [<important skills/keywords in job description but missing from resume>],
                  "summaryFeedback": "<2-3 lines of constructive, honest feedback for the candidate>"
                }
                """.formatted(resumeText, jobDescription);
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