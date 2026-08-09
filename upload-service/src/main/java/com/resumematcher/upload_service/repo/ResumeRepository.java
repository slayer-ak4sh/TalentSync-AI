package com.resumematcher.upload_service.repo;

import com.resumematcher.upload_service.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
