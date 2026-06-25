package com.finsight.documentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}
