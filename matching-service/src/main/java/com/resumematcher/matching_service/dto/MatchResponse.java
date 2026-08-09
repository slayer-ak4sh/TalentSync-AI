package com.resumematcher.matching_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MatchResponse(
        Long resumeId,
        Long jobId,
        int fitScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String gapAnalysis,
        LocalDateTime createdAt
) {}