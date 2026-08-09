package com.resumematcher.upload_service.service;

import com.resumematcher.upload_service.exception.ResumeParsingException;
import com.resumematcher.upload_service.model.Resume;
import com.resumematcher.upload_service.model.ResumeStatus;
import com.resumematcher.upload_service.repo.ResumeRepository;
import com.resumematcher.upload_service.util.ResumeTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeTextExtractor textExtractor;

    public Resume uploadResume(MultipartFile file){
        if(file.isEmpty()){
            throw new IllegalArgumentException("File is empty");
        }

        String rawText;
        try{
            rawText = textExtractor.extractText(file);
        } catch (IOException e) {
            throw new ResumeParsingException("Failed to parse file: " + e.getMessage());
        }

        if(rawText==null || rawText.isBlank()){
            throw new ResumeParsingException("Extracted text is empty or blank");
        }

        Resume resume = new Resume(
                null,
                file.getOriginalFilename(),
                rawText,
                ResumeStatus.PARSED,
                LocalDateTime.now()
        );

        return resumeRepository.save(resume);
    }
}
