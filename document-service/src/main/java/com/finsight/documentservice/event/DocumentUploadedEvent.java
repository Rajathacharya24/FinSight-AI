package com.finsight.documentservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadedEvent {
    private Long documentId;
    private String title;
    private String fileName;
    private String fileUrl;
}
