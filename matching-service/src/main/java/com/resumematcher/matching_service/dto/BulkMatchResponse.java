package com.resumematcher.matching_service.dto;

import java.util.List;

public record BulkMatchResponse(Long resumeId, List<BulkMatchItem> results, long processingTimeMs) {}
