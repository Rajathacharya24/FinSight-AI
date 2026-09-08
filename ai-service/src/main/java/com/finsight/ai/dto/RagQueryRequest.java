package com.finsight.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RagQueryRequest {
    @NotBlank
    private String question;
}
