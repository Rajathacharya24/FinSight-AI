package com.finsight.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryResponse {
    private String documentId;
    private String question;
    private String answer;
    private List<String> relevantSources;
}
