package com.resumescreener.enterpriseresumescreener.dto;

import java.util.List;

public record AnalysisResult(
        int matchPercentage,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        String summaryFeedback
) {
}