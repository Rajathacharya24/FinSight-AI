package com.finsight.documentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
