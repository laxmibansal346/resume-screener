package com.resumescreener.enterpriseresumescreener.service;

import org.springframework.stereotype.Service;

import java.util.List;
import com.resumescreener.enterpriseresumescreener.dto.HybridAnalysisResult;

@Service
public class HybridScoringService {

    private final SkillExtractionService skillExtractionService;
    private final RuleBasedMatchingService ruleBasedMatchingService;
    private final ResumeAnalysisService resumeAnalysisService;

    public HybridScoringService(SkillExtractionService skillExtractionService,
                                RuleBasedMatchingService ruleBasedMatchingService,
                                ResumeAnalysisService resumeAnalysisService) {
        this.skillExtractionService = skillExtractionService;
        this.ruleBasedMatchingService = ruleBasedMatchingService;
        this.resumeAnalysisService = resumeAnalysisService;
    }

    public HybridAnalysisResult analyze(String resumeText, String jobDescription) throws Exception {
        List<String> requiredSkills = skillExtractionService.extractSkills(jobDescription);
        return analyze(resumeText, requiredSkills, jobDescription);
    }

    public HybridAnalysisResult analyze(String resumeText, List<String> requiredSkills, String jobDescription) throws Exception {

        RuleBasedMatchingService.MatchResult ruleBasedResult =
                ruleBasedMatchingService.match(resumeText, requiredSkills);

        var aiResult = resumeAnalysisService.analyze(resumeText, jobDescription);

        int finalScore = (int) Math.round(
                (ruleBasedResult.score() * 0.6) + (aiResult.matchPercentage() * 0.4)
        );

        return new HybridAnalysisResult(
                finalScore,
                ruleBasedResult.score(),
                aiResult.matchPercentage(),
                ruleBasedResult.matchedSkills(),
                ruleBasedResult.missingSkills(),
                aiResult.summaryFeedback()
        );
    }
}