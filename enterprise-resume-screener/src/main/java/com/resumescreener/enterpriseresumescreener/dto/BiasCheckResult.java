package com.resumescreener.enterpriseresumescreener.dto;

import java.util.List;
import java.util.Map;

public record BiasCheckResult(
        List<AttributeCheck> checks,
        boolean biasFlagged,
        String biasSummary
) {
    public record AttributeCheck(
            String attribute,
            boolean flagged,
            int scoreGap,
            Map<String, Integer> scoresByVariant
    ) {}
}
