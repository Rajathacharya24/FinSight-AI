package com.finsight.document.repository;

import com.finsight.document.domain.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentContentRepository extends JpaRepository<DocumentContent, String> {
}
