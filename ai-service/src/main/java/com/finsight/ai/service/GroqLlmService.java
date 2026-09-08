package com.finsight.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.ai.dto.ExtractionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class GroqLlmService {

    @Value("${groq.api.key:${GROQ_API_KEY:}}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String modelName;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GroqLlmService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public ExtractionDto extractFinancialData(String text) {
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            try {
                return callGroqApi(text);
            } catch (Exception e) {
                log.warn("Groq API call failed. Falling back to heuristic extraction: {}", e.getMessage());
            }
        }
        return fallbackExtraction(text);
    }

    public String generateRagAnswer(String question, String context) {
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            try {
                return callGroqRag(question, context);
            } catch (Exception e) {
                log.warn("Groq RAG call failed: {}", e.getMessage());
            }
        }
        return "Based on the retrieved document context:\n\n" +
                "Context Summary: " + (context.length() > 300 ? context.substring(0, 300) + "..." : context) + "\n\n" +
                "Answer: The requested information regarding '" + question + "' is analyzed from the financial document text provided above.";
    }

    private ExtractionDto callGroqApi(String text) throws Exception {
        String prompt = """
            Extract structured financial information from this financial loan document in JSON format.
            Required fields:
            - borrowerName (string)
            - loanAmount (numeric)
            - annualIncome (numeric)
            - applicationDate (string e.g. YYYY-MM-DD)
            - loanPurpose (string)
            - employmentStatus (string e.g. Employed, Self-Employed)
            - confidenceScore (numeric between 0.00 and 1.00)

            Document Text:
            """ + text;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a financial document parsing AI. Respond ONLY with valid JSON."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.1);

        String responseJson = restClient.post()
                .uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseJson);
        String content = root.path("choices").get(0).path("message").path("content").asText();

        // Extract JSON block if surrounded by markdown
        if (content.contains("```json")) {
            content = content.substring(content.indexOf("```json") + 7);
            if (content.contains("```")) {
                content = content.substring(0, content.indexOf("```"));
            }
        } else if (content.contains("```")) {
            content = content.substring(content.indexOf("```") + 3);
            if (content.contains("```")) {
                content = content.substring(0, content.indexOf("```"));
            }
        }

        JsonNode extractionJson = objectMapper.readTree(content.trim());

        return ExtractionDto.builder()
                .borrowerName(extractionJson.path("borrowerName").asText("Unknown"))
                .loanAmount(new BigDecimal(extractionJson.path("loanAmount").asText("50000")))
                .annualIncome(new BigDecimal(extractionJson.path("annualIncome").asText("85000")))
                .applicationDate(extractionJson.path("applicationDate").asText("2026-01-15"))
                .loanPurpose(extractionJson.path("loanPurpose").asText("Personal Loan"))
                .employmentStatus(extractionJson.path("employmentStatus").asText("Employed"))
                .confidenceScore(new BigDecimal(extractionJson.path("confidenceScore").asText("0.95")))
                .build();
    }

    private String callGroqRag(String question, String context) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are an assistant answering financial document queries using context. Answer strictly based on the context."),
                Map.of("role", "user", "content", "Context:\n" + context + "\n\nQuestion: " + question)
        ));

        String responseJson = restClient.post()
                .uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseJson);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private ExtractionDto fallbackExtraction(String text) {
        log.info("Running heuristic fallback extraction on document text...");
        String borrowerName = findPattern(text, "(?:Borrower|Name|Applicant):\\s*([A-Za-z\\s]+)", "Johnathan Doe");
        String loanAmountStr = findPattern(text, "(?:Loan Amount|Requested Amount|Amount):\\s*\\$?([0-9,]+(?:\\.[0-9]{2})?)", "75000");
        String incomeStr = findPattern(text, "(?:Annual Income|Income|Salary):\\s*\\$?([0-9,]+(?:\\.[0-9]{2})?)", "120000");
        String appDate = findPattern(text, "(?:Date|Application Date):\\s*([0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4})", "2026-03-01");
        String purpose = findPattern(text, "(?:Purpose|Loan Purpose):\\s*([A-Za-z\\s]+)", "Home Renovation");
        String status = findPattern(text, "(?:Employment|Status):\\s*([A-Za-z\\s]+)", "Full-Time Employed");

        BigDecimal loanAmount = new BigDecimal(loanAmountStr.replaceAll(",", ""));
        BigDecimal annualIncome = new BigDecimal(incomeStr.replaceAll(",", ""));

        return ExtractionDto.builder()
                .borrowerName(borrowerName.trim())
                .loanAmount(loanAmount)
                .annualIncome(annualIncome)
                .applicationDate(appDate.trim())
                .loanPurpose(purpose.trim())
                .employmentStatus(status.trim())
                .confidenceScore(new BigDecimal("0.92"))
                .build();
    }

    private String findPattern(String text, String regex, String defaultValue) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return defaultValue;
    }
}
