package com.finsight.documentservice.repository;

import com.finsight.documentservice.model.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentContentRepository extends JpaRepository<DocumentContent, Long> {

    java.util.Optional<DocumentContent> findByDocumentId(Long documentId);
}
