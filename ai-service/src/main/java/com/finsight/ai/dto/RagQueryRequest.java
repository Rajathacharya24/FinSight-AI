package com.finsight.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class RagQueryRequest {
    @NotBlank
    private String question;

    public RagQueryRequest() {}
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
