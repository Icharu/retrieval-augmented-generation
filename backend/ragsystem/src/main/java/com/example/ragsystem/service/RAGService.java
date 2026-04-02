package com.example.ragsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

        private final VectorStore vectorStore;
        private final ChatClient.Builder chatClientBuilder;

        private static final String PROMPT_TEMPLATE = """
                        You are a helpful AI assistant. Answer the user's question based ONLY on the provided context below.
                        If you don't know the answer based on the context, just say that you don't know, do not try to make up an answer.

                        Context:
                        {context}

                        Question:
                        {question}
                        """;

        public Flux<String> generateAnswer(String question) {
                log.info("Received query: {}", question);

                List<Document> similarDocuments = vectorStore.similaritySearch(
                                SearchRequest.builder().query(question).topK(4).build());

                String context = similarDocuments.stream()
                                .map(Document::getText)
                                .collect(Collectors.joining("\n\n"));

                log.info("Retrieved {} relevant documents for context", similarDocuments.size());

                PromptTemplate promptTemplate = new PromptTemplate(PROMPT_TEMPLATE);
                Prompt prompt = promptTemplate.create(Map.of(
                                "context", context,
                                "question", question));

                ChatClient chatClient = chatClientBuilder.build();
                return chatClient.prompt(prompt)
                                .stream()
                                .content();
        }
}
