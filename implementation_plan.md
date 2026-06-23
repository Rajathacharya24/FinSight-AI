# Implement remaining Document Service features

## Goal Description
Finish the PDF upload and extraction flow by adding the missing DTO, Flyway migration, unit and integration tests, and ensure the application starts correctly with PostgreSQL.

## User Review Required
- Approve the structure of `DocumentUploadResponse` DTO (fields: documentId, uploadedAt, extractedChars, status).
- Confirm the migration script name and any additional columns for `document_content` (currently just `document_id` and `content`).
- Decide whether to keep the uploaded PDF file after extraction (currently retained).

## Open Questions
> [!IMPORTANT]
> Do you want the `status` field in the response to reflect the final status (`EXTRACTED`) or the intermediate upload status (`UPLOADED`)?

> [!IMPORTANT]
> Should the integration test spin up a real PostgreSQL container (via Testcontainers) or rely on an in‑memory DB?

## Proposed Changes
---
### DTOs
- **[NEW]** `DocumentUploadResponse` DTO with fields:
  - `Long documentId`
  - `Instant uploadedAt`
  - `int extractedChars`
  - `String status` (e.g., `EXTRACTED`)

---
### Database Migration
- **[NEW]** Flyway script `V2__create_document_content_table.sql`:
```sql
CREATE TABLE document_content (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    content TEXT NOT NULL
);
```

---
### Service Layer
- Ensure `DocumentService.uploadDocument` returns the new `DocumentUploadResponse` (already present).

---
### Controller
- Update `DocumentController.uploadDocument` to return `ResponseEntity<DocumentUploadResponse>` and delegate to `DocumentService` (already adjusted but needs DTO import).

---
### Tests
#### Unit Test for PdfExtractorService
- **[NEW]** `PdfExtractorServiceTest` using a sample PDF placed in `src/test/resources/sample.pdf`.
- Verify that `extractText` returns non‑empty string and matches expected character count.

#### Integration Test for Upload Endpoint
- **[NEW]** `DocumentControllerIntegrationTest` (SpringBootTest) that:
  1. Starts the application with an embedded PostgreSQL (Testcontainers) or H2 in PostgreSQL compatibility mode.
  2. Sends a multipart `POST /api/v1/documents` with a small PDF and JSON metadata.
  3. Asserts HTTP 200 response and that the JSON body contains `documentId`, `uploadedAt`, and `extractedChars`.
  4. Queries the DB to confirm a row exists in `document_content` with matching `document_id`.

---
### Verification Plan
- Run `mvn test` locally; ensure all unit and integration tests pass.
- Start the service with `mvn spring-boot:run` pointing to a local PostgreSQL container and verify the API works via `curl`.
- Check Flyway logs that both V1 and V2 migrations are applied.

## Automated Tests
- `mvn test`

## Manual Verification
- Use `curl -F "metadata={\"title\":\"Test\"};type=application/json" -F "file=@sample.pdf" http://localhost:8080/api/v1/documents` to manually verify the endpoint returns the expected JSON.
