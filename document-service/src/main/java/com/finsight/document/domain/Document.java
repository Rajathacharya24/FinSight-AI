package com.finsight.document.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String storageLocation;

    @Column(nullable = false)
    private String status;

    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Builder.Default
    private ZonedDateTime updatedAt = ZonedDateTime.now();
}
