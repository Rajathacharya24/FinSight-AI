package com.finsight.workflowservice.model;

/**
 * Full workflow state machine for FinSight-AI loan processing.
 *
 * <pre>
 * CREATED → UPLOADED → SCANNING → EXTRACTING_TEXT → AI_EXTRACTION
 *     → REVIEW_PENDING → APPROVED / REJECTED / MANUAL_REVIEW
 *     → NOTIFIED
 *
 * Terminal error states: FAILED, REJECTED_MALWARE, NEEDS_OCR
 * </pre>
 */
public enum WorkflowStatus {

    // ── Entry state ──────────────────────────────────────────────
    /** Workflow record created; waiting for document upload to complete. */
    CREATED,

    // ── Processing pipeline ──────────────────────────────────────
    /** Document has been uploaded to object storage; scan not yet started. */
    UPLOADED,

    /** ClamAV / malware scan in progress. */
    SCANNING,

    /** PDF text extraction (PDFBox) in progress. */
    EXTRACTING_TEXT,

    /** AI extraction pipeline (RAG + LLM) running. */
    AI_EXTRACTION,

    // ── Review states ─────────────────────────────────────────────
    /** Extraction complete; awaiting human reviewer decision. */
    REVIEW_PENDING,

    /** Reviewer approved the application. */
    APPROVED,

    /** Reviewer rejected the application. */
    REJECTED,

    /** Reviewer requested additional information before making a decision. */
    MANUAL_REVIEW,

    // ── Completion ─────────────────────────────────────────────────
    /** Decision notification has been sent to the applicant. Terminal success state. */
    NOTIFIED,

    // ── Legacy / backward-compat ───────────────────────────────────
    /** Deprecated: use APPROVED or REJECTED instead. Kept for backward compat with existing DB rows. */
    COMPLETED,

    // ── Error states ───────────────────────────────────────────────
    /** Malware detected during scanning. Document quarantined. */
    REJECTED_MALWARE,

    /** No extractable text found (scanned/image-only PDF). Awaiting OCR. */
    NEEDS_OCR,

    /** Unrecoverable failure — see failure_reason column. */
    FAILED,

    /** Kept for legacy; maps to FAILED in new code. */
    IN_PROGRESS
}
