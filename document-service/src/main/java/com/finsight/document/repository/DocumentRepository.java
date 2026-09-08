package com.finsight.document.repository;

import com.finsight.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByUserIdOrderByCreatedAtDesc(String userId);
}
