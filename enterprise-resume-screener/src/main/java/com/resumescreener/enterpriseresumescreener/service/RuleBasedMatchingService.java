package com.resumescreener.enterpriseresumescreener.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RuleBasedMatchingService {

    private static final List<String> IGNORED_WORDS = Arrays.asList(
            "and", "or", "the", "for", "with", "apis", "api",
            "design", "designing", "engineering", "computer", "software",
            "system", "systems", "development", "developing", "management",
            "architecture", "database", "databases", "caching", "testing",
            "tools", "platform", "platforms", "framework", "frameworks",
            "knowledge", "experience", "understanding", "strong", "basic"
    );
    public MatchResult match(String resumeText, List<String> requiredSkills) {
        String normalizedResume = resumeText.toLowerCase();

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String skill : requiredSkills) {
            if (isSkillPresent(normalizedResume, skill)) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }

        int score = requiredSkills.isEmpty()
                ? 0
                : (int) Math.round((matched.size() * 100.0) / requiredSkills.size());

        return new MatchResult(score, matched, missing);
    }

    private boolean isSkillPresent(String normalizedResume, String skill) {
        String normalizedSkill = skill.toLowerCase();

        if (normalizedResume.contains(normalizedSkill)) {
            return true;
        }

        String[] words = normalizedSkill.split("\\s+");
        List<String> significantWords = new ArrayList<>();

        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-z0-9]", "");
            if (cleanWord.length() > 3 && !IGNORED_WORDS.contains(cleanWord)) {
                significantWords.add(cleanWord);
            }
        }

        if (significantWords.isEmpty()) {
            return false;
        }

        for (String word : significantWords) {
            if (normalizedResume.contains(word)) {
                return true;
            }
        }

        return false;
    }

    public record MatchResult(int score, List<String> matchedSkills, List<String> missingSkills) {}
}