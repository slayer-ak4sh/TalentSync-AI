package com.resumematcher.matching_service.service;

import com.resumematcher.matching_service.client.UploadServiceClient;
import com.resumematcher.matching_service.dto.*;
import com.resumematcher.matching_service.model.MatchResult;
import com.resumematcher.matching_service.repo.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UploadServiceClient uploadServiceClient;
    private final GeminiClient geminiClient;
    private final MatchResultRepository matchResultRepository;
    private final ExecutorService matchExecutor;

    public MatchResponse match(MatchRequest request) {
        // (unchanged from Step 6 — keep your existing single-match method)
        Optional<MatchResult> existing = matchResultRepository
                .findByResumeIdAndJobId(request.resumeId(), request.jobId());

        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        ResumeDto resume = uploadServiceClient.getResume(request.resumeId());
        JobDto job = uploadServiceClient.getJob(request.jobId());

        if (resume == null || job == null) {
            throw new IllegalArgumentException("Resume or Job not found");
        }

        MatchAnalysis analysis = geminiClient.analyzeMatch(resume.rawText(), job.rawText());

        MatchResult result = new MatchResult(
                null, resume.id(), job.id(), analysis.fitScore(),
                analysis.matchedSkills(), analysis.missingSkills(),
                analysis.gapAnalysis(), LocalDateTime.now()
        );

        return toResponse(matchResultRepository.save(result));
    }

    public BulkMatchResponse bulkMatch(BulkMatchRequest request) {
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<BulkMatchItem>> futures = request.jobIds().stream()
                .map(jobId -> CompletableFuture.supplyAsync(
                        () -> matchSingleForBulk(request.resumeId(), jobId),
                        matchExecutor))
                .toList();

        List<BulkMatchItem> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long durationMs = System.currentTimeMillis() - startTime;

        return new BulkMatchResponse(request.resumeId(), results, durationMs);
    }

    private BulkMatchItem matchSingleForBulk(Long resumeId, Long jobId) {
        try {
            MatchResponse response = match(new MatchRequest(resumeId, jobId));
            // Need the saved entity's ID — fetch it since match() returns a DTO without id
            MatchResult saved = matchResultRepository
                    .findByResumeIdAndJobId(resumeId, jobId)
                    .orElseThrow();
            return new BulkMatchItem(jobId, response.fitScore(), saved.getId(), null);
        } catch (Exception e) {
            // Isolate failures — one bad job shouldn't kill the whole batch
            return new BulkMatchItem(jobId, -1, null, e.getMessage());
        }
    }

    private MatchResponse toResponse(MatchResult result) {
        return new MatchResponse(
                result.getResumeId(), result.getJobId(), result.getFitScore(),
                result.getMatchedSkills(), result.getMissingSkills(),
                result.getGapAnalysis(), result.getCreatedAt()
        );
    }
}