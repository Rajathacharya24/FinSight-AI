package com.finsight.documentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentUploadRequest {
    @NotBlank(message = "Document title cannot be blank")
    @Size(min = 3, max = 100, message = "Document title must be between 3 and 100 characters")
    private String title;

    private String description;

    private Long userId;
}
