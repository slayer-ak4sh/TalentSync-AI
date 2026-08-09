package com.resumematcher.upload_service.service;

import com.resumematcher.upload_service.model.Resume;
import com.resumematcher.upload_service.repo.ResumeRepository;
import com.resumematcher.upload_service.util.ResumeTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeTextExtractor textExtractor;

    @InjectMocks
    private ResumeService resumeService;

    @Test
    void uploadResume_throwsException_whenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> resumeService.uploadResume(emptyFile));
    }

    @Test
    void uploadResume_savesResume_whenValidTextFileProvided() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "Java developer".getBytes());

        when(textExtractor.extractText(file)).thenReturn("Java developer");
        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> inv.getArgument(0));

        Resume result = resumeService.uploadResume(file);

        assertEquals("Java developer", result.getRawText());
        verify(resumeRepository, times(1)).save(any(Resume.class));
    }
}
