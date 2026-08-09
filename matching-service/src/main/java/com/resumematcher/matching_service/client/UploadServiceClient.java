package com.resumematcher.matching_service.client;

import com.resumematcher.matching_service.dto.JobDto;
import com.resumematcher.matching_service.dto.ResumeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UploadServiceClient {

    private final RestClient restClient;

    @Value("${upload.service.url}")
    private String uploadServiceUrl;

    public ResumeDto getResume(Long resumeId){
        return restClient.get()
                .uri(uploadServiceUrl + "/api/v1/resumes/{id}", resumeId)
                .retrieve()
                .body(ResumeDto.class);
    }

    public JobDto getJob(Long jobId){
        return restClient.get()
                .uri(uploadServiceUrl + "/api/v1/jobs/{id}", jobId)
                .retrieve()
                .body(JobDto.class);
    }
}
