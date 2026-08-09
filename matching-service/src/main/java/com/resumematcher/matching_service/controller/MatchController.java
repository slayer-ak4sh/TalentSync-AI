    package com.resumematcher.matching_service.controller;

    import com.resumematcher.matching_service.dto.BulkMatchRequest;
    import com.resumematcher.matching_service.dto.BulkMatchResponse;
    import com.resumematcher.matching_service.dto.MatchRequest;
    import com.resumematcher.matching_service.dto.MatchResponse;
    import com.resumematcher.matching_service.model.MatchResult;
    import com.resumematcher.matching_service.repo.MatchResultRepository;
    import com.resumematcher.matching_service.service.GeminiClient;
    import com.resumematcher.matching_service.service.MatchingService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/v1/match")
    @RequiredArgsConstructor
    public class MatchController {

        private final MatchingService matchingService;
        private final MatchResultRepository matchResultRepository;

        @PostMapping
        public ResponseEntity<MatchResponse> match(@RequestBody MatchRequest request) {
            return ResponseEntity.ok(matchingService.match(request));
        }

        @GetMapping("/{id}")
        public ResponseEntity<MatchResult> getById(@PathVariable Long id){
            return matchResultRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/resume/{resumeId}")
        public List<MatchResult> getByResume(@PathVariable Long resumeId) {
            return matchResultRepository.findByResumeId(resumeId);
        }

        @GetMapping("/job/{jobId}")
        public List<MatchResult> getByJob(@PathVariable Long jobId) {
            return matchResultRepository.findByJobId(jobId);
        }

        @GetMapping("/run")
        public ResponseEntity<MatchResponse> matchViaGet(@RequestParam Long resumeId, @RequestParam Long jobId) {
            return ResponseEntity.ok(matchingService.match(new MatchRequest(resumeId, jobId)));
        }

        @PostMapping("/bulk")
        public ResponseEntity<BulkMatchResponse> bulkMatch(@RequestBody BulkMatchRequest request) {
            return ResponseEntity.ok(matchingService.bulkMatch(request));
        }
    }
