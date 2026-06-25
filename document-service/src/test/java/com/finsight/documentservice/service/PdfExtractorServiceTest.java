package com.finsight.documentservice.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfExtractorServiceTest {

    @Test
    void extractTextFromPdf(@TempDir Path tempDir) throws IOException {
        // Create a temporary PDF with known text
        File pdfFile = tempDir.resolve("sample.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.beginText();
                contents.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contents.newLineAtOffset(100, 700);
                contents.showText("Hello PDFBox");
                contents.endText();
            }
            doc.save(pdfFile);
        }

        PdfExtractorService extractor = new PdfExtractorService();
        String extracted = extractor.extractText(pdfFile);
        assertTrue(extracted.contains("Hello PDFBox"), "Extracted text should contain the known string");
    }
}
