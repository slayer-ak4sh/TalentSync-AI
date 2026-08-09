package com.resumematcher.upload_service.service;

import com.resumematcher.upload_service.dto.JobDescriptionRequest;
import com.resumematcher.upload_service.model.JobDescription;
import com.resumematcher.upload_service.repo.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;

    public JobDescription createJob(JobDescriptionRequest request) {
        JobDescription job = new JobDescription(
                null,
                request.getTitle(),
                request.getCompany(),
                request.getRawText(),
                LocalDateTime.now()
        );
        return jobDescriptionRepository.save(job);
    }
}
