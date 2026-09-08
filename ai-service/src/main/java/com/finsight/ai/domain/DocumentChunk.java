package com.finsight.ai.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "document_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String chunkText;

    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
