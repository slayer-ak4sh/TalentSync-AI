package com.resumematcher.upload_service.controller;


import com.resumematcher.upload_service.config.FileConstraints;
import com.resumematcher.upload_service.model.Resume;
import com.resumematcher.upload_service.repo.ResumeRepository;
import com.resumematcher.upload_service.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Resume> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file.getSize() > FileConstraints.MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        Resume saved = resumeService.uploadResume(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getById(@PathVariable Long id) {
        return resumeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Resume> getAll() {
        return resumeRepository.findAll();
    }
}
