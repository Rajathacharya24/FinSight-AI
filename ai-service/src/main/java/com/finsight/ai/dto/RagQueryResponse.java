package com.finsight.ai.dto;

import java.util.List;

public class RagQueryResponse {
    private String documentId;
    private String question;
    private String answer;
    private List<String> relevantSources;

    public RagQueryResponse() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<String> getRelevantSources() { return relevantSources; }
    public void setRelevantSources(List<String> relevantSources) { this.relevantSources = relevantSources; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String documentId;
        private String question;
        private String answer;
        private List<String> relevantSources;

        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder question(String question) { this.question = question; return this; }
        public Builder answer(String answer) { this.answer = answer; return this; }
        public Builder relevantSources(List<String> relevantSources) { this.relevantSources = relevantSources; return this; }

        public RagQueryResponse build() {
            RagQueryResponse r = new RagQueryResponse();
            r.setDocumentId(documentId);
            r.setQuestion(question);
            r.setAnswer(answer);
            r.setRelevantSources(relevantSources);
            return r;
        }
    }
}
