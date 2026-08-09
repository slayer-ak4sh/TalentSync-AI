package com.resumematcher.upload_service.util;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ResumeTextExtractor {

    public String extractText(MultipartFile file) throws IOException{

        String fileName = file.getOriginalFilename();

        if(fileName == null){
            throw new IllegalArgumentException("File name is missing");
        }

        if(fileName.toLowerCase().endsWith(".pdf")){
            return extractFromPdf(file);
        }else if(fileName.toLowerCase().endsWith(".txt")){
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }else{
            throw new IllegalArgumentException("Unsupported file type.Only PDF and TXT allowed");
        }
    }

    private String extractFromPdf(MultipartFile file) {
        try(PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
