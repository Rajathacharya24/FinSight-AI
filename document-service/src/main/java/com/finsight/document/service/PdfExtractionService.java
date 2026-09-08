package com.finsight.document.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class PdfExtractionService {

    public String extractTextFromPdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF file {}", text.length(), file.getOriginalFilename());
            return text != null ? text.trim() : "";
        } catch (IOException e) {
            log.error("Failed to extract text from PDF file {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
}
