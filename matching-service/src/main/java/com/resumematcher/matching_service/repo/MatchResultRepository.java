package com.resumematcher.matching_service.repo;

import com.resumematcher.matching_service.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    Optional<MatchResult> findByResumeIdAndJobId(Long resumeId, Long jobId);

    List<MatchResult> findByResumeId(Long resumeId);

    List<MatchResult> findByJobId(Long jobId);
}
