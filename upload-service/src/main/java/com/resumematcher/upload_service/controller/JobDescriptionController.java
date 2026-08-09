package com.resumematcher.upload_service.controller;

import com.resumematcher.upload_service.dto.JobDescriptionRequest;
import com.resumematcher.upload_service.model.JobDescription;
import com.resumematcher.upload_service.repo.JobDescriptionRepository;
import com.resumematcher.upload_service.service.JobDescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;
    private final JobDescriptionRepository jobDescriptionRepository;

    @PostMapping
    public ResponseEntity<JobDescription> createJob(@Valid @RequestBody JobDescriptionRequest request) {
        JobDescription saved = jobDescriptionService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDescription> getById(@PathVariable Long id) {
        return jobDescriptionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<JobDescription> getAll() {
        return jobDescriptionRepository.findAll();
    }
}
