package com.resumematcher.upload_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobDescriptionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String company;

    @NotBlank(message = "Job description text is required")
    private String rawText;
}
