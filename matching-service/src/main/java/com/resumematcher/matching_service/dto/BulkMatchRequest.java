package com.resumematcher.matching_service.dto;

import java.util.List;

public record BulkMatchRequest(Long resumeId, List<Long> jobIds) {}
