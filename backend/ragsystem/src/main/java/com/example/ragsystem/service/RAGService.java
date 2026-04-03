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
                        Você é um assistente de IA prestativo e preciso.
                        Responda à pergunta do usuário baseando-se APENAS no contexto fornecido abaixo.
                        Se o contexto não contiver a informação necessária, diga explicitamente que você não encontrou essa informação nos documentos enviados.
                        Não invente fatos nem use conhecimento externo que não esteja no contexto.

                        Formate sua resposta usando Markdown estrito:
                        - Use ## para títulos de seção e ### para subseções
                        - Use **texto** para negrito
                        - Use listas com - para itens
                        - Separe SEMPRE parágrafos e seções com uma linha em branco

                        REGRA OBRIGATÓRIA para fórmulas matemáticas:
                        Use SEMPRE a sintaxe LaTeX com cifrão para qualquer expressão matemática.
                        Para inline use $E = mc^2$ e para blocos use $$E = mc^2$$ em linha separada.
                        Use comandos LaTeX como gamma, frac, sqrt, left, right para expressoes complexas.
                        NUNCA escreva fórmulas como texto puro. SEMPRE use $ ou $$ ao redor.

                        Contexto:
                        {context}

                        Pergunta:
                        {question}
                        """;

        public Flux<String> generateAnswer(String question) {
                log.info("Received query: {}", question);

                List<Document> similarDocuments = vectorStore.similaritySearch(
                                SearchRequest.builder().query(question).topK(4).build());

                String context = similarDocuments.stream()
                                .map(Document::getText)
                                .filter(text -> text != null)
                                .collect(Collectors.joining("\n\n"));

                log.info("Retrieved {} relevant documents for context", similarDocuments.size());

                PromptTemplate promptTemplate = new PromptTemplate(PROMPT_TEMPLATE);
                Prompt prompt = promptTemplate.create(Map.of(
                                "context", context,
                                "question", question));

                ChatClient chatClient = chatClientBuilder.build();
                return chatClient.prompt(prompt)
                                .stream()
                                .content()
                                // Filter out null chunks that some models emit
                                .filter(chunk -> chunk != null);
        }
}