package com.finsight.document.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "document_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentContent {
    @Id
    private String documentId;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private Integer charCount;
    private Integer wordCount;

    @Builder.Default
    private ZonedDateTime extractedAt = ZonedDateTime.now();
}
