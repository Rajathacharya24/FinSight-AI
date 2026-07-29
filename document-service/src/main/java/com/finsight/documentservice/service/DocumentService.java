package com.finsight.documentservice.service;

import com.finsight.documentservice.config.DocumentStorageProperties;
import com.finsight.documentservice.config.MinioConfig;
import com.finsight.documentservice.dto.DocumentContentResponse;
import com.finsight.documentservice.dto.DocumentUploadRequest;
import com.finsight.documentservice.dto.DocumentUploadResponse;
import com.finsight.documentservice.model.Document;
import com.finsight.documentservice.model.DocumentContent;
import com.finsight.documentservice.repository.DocumentContentRepository;
import com.finsight.documentservice.repository.DocumentRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

/**
 * Document upload and retrieval service.
 *
 * <p>Storage strategy (resolved at startup):
 * <ul>
 *   <li><strong>MinIO</strong> — when {@code minio.enabled=true} and a {@link MinioClient} bean is present</li>
 *   <li><strong>Local disk</strong> — fallback; uses {@code document.upload-dir}</li>
 * </ul>
 *
 * <p>The uploaded file is first written to a temporary path so that the text extractor can read it
 * as a {@link java.io.File} after the multipart stream has been consumed by the store operation.
 */
@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final PdfExtractorService pdfExtractorService;
    private final DocumentStorageProperties storageProperties;
    private final VirusScannerService virusScannerService;
    private final MinioConfig.MinioProperties minioProperties;
    private final MinioClient minioClient; // null when MinIO is disabled

    public DocumentService(DocumentRepository documentRepository,
                           DocumentContentRepository documentContentRepository,
                           PdfExtractorService pdfExtractorService,
                           DocumentStorageProperties storageProperties,
                           VirusScannerService virusScannerService,
                           MinioConfig.MinioProperties minioProperties,
                           // Optional — Spring injects null if no MinioClient bean exists
                           @org.springframework.beans.factory.annotation.Autowired(required = false)
                           MinioClient minioClient) {
        this.documentRepository          = documentRepository;
        this.documentContentRepository   = documentContentRepository;
        this.pdfExtractorService         = pdfExtractorService;
        this.storageProperties           = storageProperties;
        this.virusScannerService         = virusScannerService;
        this.minioProperties             = minioProperties;
        this.minioClient                 = minioClient;
    }

    @PostConstruct
    void init() {
        if (minioClient != null && minioProperties.enabled()) {
            ensureBucketExists();
            log.info("[DocumentService] Storage backend: MinIO → {}/{}", minioProperties.endpoint(), minioProperties.bucket());
        } else {
            log.info("[DocumentService] Storage backend: local disk → {}", storageProperties.getUploadDir());
        }
    }

    /**
     * Full upload pipeline:
     * 1. Validate type, size.
     * 2. Virus scan.
     * 3. Write to temp file (avoids consuming InputStream twice).
     * 4. Persist metadata (UPLOADED).
     * 5. Store file in MinIO or local disk.
     * 6. Extract text.
     * 7. Persist extracted content.
     * 8. Update status to EXTRACTED.
     * 9. Return response.
     */
    public DocumentUploadResponse uploadDocument(@Valid DocumentUploadRequest request,
                                                 MultipartFile file) throws IOException {

        // 0️⃣ Validate
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            throw new IllegalArgumentException("File size exceeds limit of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        // Magic byte validation (check for %PDF-)
        try (InputStream is = file.getInputStream()) {
            byte[] magic = new byte[5];
            if (is.read(magic) < 5 || !new String(magic).startsWith("%PDF-")) {
                throw new IllegalArgumentException("Invalid file format: Not a valid PDF document");
            }
        }

        // 0.5️⃣ Virus scan
        if (!virusScannerService.scanFile(file)) {
            throw new IllegalArgumentException("File rejected by malware scanner");
        }

        // Write multipart data to a temp file once — reused for store + extract
        Path tmpFile = Files.createTempFile("finsight-upload-", ".pdf");
        try {
            Files.copy(file.getInputStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING);

            // 1️⃣ Save metadata
            Document document = new Document();
            document.setUserId(request.getUserId() != null ? request.getUserId() : 0L);
            document.setFileName(file.getOriginalFilename());
            document.setStatus("UPLOADED");
            document.setUploadedAt(Instant.now());
            document = documentRepository.save(document);

            // 2️⃣ Store file
            String objectKey = document.getId() + "-" + file.getOriginalFilename();
            if (minioClient != null && minioProperties.enabled()) {
                try (InputStream in = Files.newInputStream(tmpFile)) {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(objectKey)
                            .stream(in, file.getSize(), -1)
                            .contentType("application/pdf")
                            .build());
                }
                document.setStorageKey(minioProperties.bucket() + "/" + objectKey);
                log.info("[DocumentService] Stored in MinIO: {}/{}", minioProperties.bucket(), objectKey);
            } else {
                Path uploadDir = Path.of(storageProperties.getUploadDir());
                Files.createDirectories(uploadDir);
                Path target = uploadDir.resolve(objectKey);
                Files.copy(tmpFile, target, StandardCopyOption.REPLACE_EXISTING);
                document.setStorageKey(target.toString());
                log.info("[DocumentService] Stored on disk: {}", target);
            }
            documentRepository.save(document);

            // 3️⃣ Extract text
            String extractedText = pdfExtractorService.extractText(tmpFile.toFile());

            // 4️⃣ Persist extracted content
            DocumentContent content = new DocumentContent();
            content.setDocumentId(document.getId());
            content.setContent(extractedText);
            documentContentRepository.save(content);

            // 5️⃣ Update status
            document.setStatus("EXTRACTED");
            documentRepository.save(document);

            // 6️⃣ Build response
            DocumentUploadResponse response = new DocumentUploadResponse();
            response.setDocumentId(document.getId());
            response.setUploadedAt(document.getUploadedAt());
            response.setExtractedChars(extractedText != null ? extractedText.length() : 0);
            response.setStatus(document.getStatus());
            return response;

        } catch (Exception e) {
            if (e instanceof IOException ioe) throw ioe;
            throw new RuntimeException("Document upload failed: " + e.getMessage(), e);
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

    public DocumentContentResponse getDocumentContent(Long documentId) {
        DocumentContent content = documentContentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found for document: " + documentId));

        DocumentContentResponse response = new DocumentContentResponse();
        response.setDocumentId(documentId);
        response.setContent(content.getContent());
        return response;
    }

    // ─────────────────────────── MinIO helpers ───────────────────────────

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.bucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.bucket()).build());
                log.info("[DocumentService] Created MinIO bucket: {}", minioProperties.bucket());
            }
        } catch (Exception e) {
            log.warn("[DocumentService] Could not verify/create MinIO bucket: {}", e.getMessage());
        }
    }
}
