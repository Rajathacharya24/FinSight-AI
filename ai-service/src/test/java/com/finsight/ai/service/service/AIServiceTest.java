package com.finsight.ai.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.ai.service.dto.ExtractionResponse;
import com.finsight.ai.service.repository.LoanApplicationExtractionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.ai.chat.prompt.Prompt;

class AIServiceTest {

    @Test
    void extracts_fields_from_raw_text() {
        LoanApplicationExtractionRepository repo = mock(LoanApplicationExtractionRepository.class);
        when(repo.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        ChatModel chatModel = mock(ChatModel.class);
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        String json = "{\"applicantName\":\"Jane Doe\",\"income\":\"95000\",\"address\":\"742 Evergreen Terrace\",\"loanAmount\":\"180000\"}";
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(json))));
        when(chatModel.call(isA(Prompt.class))).thenReturn(chatResponse);

        AIService service = new AIService(chatClient, repo, new ObjectMapper());

        ExtractionResponse out = service.extractAndPersist(42L, "dummy text");

        assertThat(out.getApplicantName()).isEqualTo("Jane Doe");
        assertThat(out.getIncome()).isEqualTo("95000");
        assertThat(out.getAddress()).isEqualTo("742 Evergreen Terrace");
        assertThat(out.getLoanAmount()).isEqualTo("180000");
        assertThat(out.getStatus()).isEqualTo("EXTRACTED");
        assertThat(out.getDocumentId()).isEqualTo(42L);
    }

    @Test
    void returns_nulls_for_missing_fields() {
        LoanApplicationExtractionRepository repo = mock(LoanApplicationExtractionRepository.class);
        when(repo.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        ChatModel chatModel = mock(ChatModel.class);
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        String json = "{\"applicantName\":null,\"income\":null,\"address\":null,\"loanAmount\":null}";
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(json))));
        when(chatModel.call(isA(Prompt.class))).thenReturn(chatResponse);

        AIService service = new AIService(chatClient, repo, new ObjectMapper());

        ExtractionResponse out = service.extractAndPersist(1L, "no relevant content here");

        assertThat(out.getApplicantName()).isNull();
        assertThat(out.getIncome()).isNull();
        assertThat(out.getStatus()).isEqualTo("EXTRACTED");
    }
}
