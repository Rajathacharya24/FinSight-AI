package com.finsight.documentservice.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfExtractorService {

    /**
     * Extracts plain text from a PDF file using Apache PDFBox 3.x.
     *
     * @param pdfFile the PDF file on disk
     * @return the extracted text (may be empty if the PDF has no text content)
     * @throws IOException if the file cannot be read or parsed
     */
    public String extractText(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
