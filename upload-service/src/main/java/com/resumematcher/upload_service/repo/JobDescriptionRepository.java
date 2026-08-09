package com.resumematcher.upload_service.repo;

import com.resumematcher.upload_service.model.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDescriptionRepository extends JpaRepository<JobDescription,Long> {
}
