package com.finsight.documentservice.controller;

import com.finsight.documentservice.dto.DocumentUploadRequest;
import com.finsight.documentservice.dto.DocumentUploadResponse;
import com.finsight.documentservice.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.config.enabled=false",
        "spring.cloud.compatibility-verifier.enabled=false",
        "eureka.client.enabled=false",
        "management.tracing.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "document.upload-dir=${java.io.tmpdir}/finsight-test-uploads"
})
class DocumentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadDocument_shouldReturnDto() throws Exception {
        // Build a real small PDF in memory
        ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(100, 700);
                cs.showText("Integration test content");
                cs.endText();
            }
            doc.save(pdfBytes);
        }

        // Prepare metadata JSON part
        DocumentUploadRequest req = new DocumentUploadRequest();
        req.setTitle("Test Document");
        req.setDescription("Test description");
        byte[] metadataJson = objectMapper.writeValueAsBytes(req);
        MockMultipartFile metadataPart = new MockMultipartFile(
                "metadata", "metadata.json", MediaType.APPLICATION_JSON_VALUE, metadataJson);

        // Prepare file part
        MockMultipartFile filePart = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfBytes.toByteArray());

        mockMvc.perform(multipart("/api/v1/documents")
                        .file(metadataPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(result -> {
                    DocumentUploadResponse resp = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            DocumentUploadResponse.class);
                    assertThat(resp.getDocumentId()).isNotNull();
                    assertThat(resp.getUploadedAt()).isNotNull();
                    assertThat(resp.getExtractedChars()).isGreaterThan(0);
                    assertThat(resp.getStatus()).isEqualTo("EXTRACTED");
                });
    }
}
