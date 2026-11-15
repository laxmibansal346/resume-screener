package com.resumescreener.enterpriseresumescreener.dto;

import java.util.List;

public record HybridAnalysisResult(
        int finalScore,
        int ruleBasedScore,
        int aiScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String summaryFeedback
) {}