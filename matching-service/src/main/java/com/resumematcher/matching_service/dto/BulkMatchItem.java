package com.resumematcher.matching_service.dto;

public record BulkMatchItem(Long jobId, int fitScore, Long matchId, String error) {}
