package com.resumematcher.matching_service.dto;

import java.util.List;

public record MatchAnalysis(
        List<String> matchedSkills,
        List<String> missingSkills,
        int fitScore,
        String gapAnalysis
) {}
