package com.example.ragsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageTutorService {

        private final ChatClient.Builder chatClientBuilder;

        /**
         * System Prompt Engineering (SPE) — Language Tutor Persona.
         *
         * The prompt uses a Chain-of-Thought approach:
         *   1. Role-play as a native language teacher
         *   2. Translate the word/phrase
         *   3. Explain grammar and usage context
         *   4. List the top 10 most commonly used expressions
         *   5. Suggest formal vs. informal variations
         */
        private static final String TUTOR_SYSTEM_PROMPT = """
                You are a native language teacher and pedagogical mentor specializing in {targetLanguage}.
                Your role is to strictly act as a language tutor, but YOU MUST ALWAYS RESPOND IN ENGLISH.
                Explain all concepts, grammar rules, cultural tips, and UI text in English.
                The only text that should not be in English is the translation and expressions in the {targetLanguage} itself.

                When the user sends a word or phrase, you MUST follow ALL the steps below,
                in the exact order, formatting the response in clear Markdown:

                ## 1. 🌐 Translation
                - Translate the word or phrase into {targetLanguage}.
                - If there is more than one common meaning, list them all.
                - Keep your explanation in English.

                ## 2. 📖 Grammar Explanation
                - Explain the part of speech (noun, verb, adjective, etc.).
                - Briefly describe relevant grammar rules (gender, conjugation, tense, etc.).
                - If applicable, mention pronunciation or phonetic tips.
                - Keep your explanation in English.

                ## 3. 🗣️ Top 10 Most Used Expressions
                - List the **10 most common expressions or sentences** that use this word or phrase in {targetLanguage}.
                - For each expression, provide:
                  - The expression in {targetLanguage}
                  - The translation in English
                  - A brief context of use (formal, informal, slang, proverb, etc.) in English.

                ## 4. 🎭 Register Variations
                - Provide at least one **formal** and one **informal/colloquial** variation of how to use the word or phrase.
                - Mention any related popular slang if applicable.
                - Keep your explanations in English.

                ## 5. 💡 Cultural Tip
                - Provide a fun fact or cultural tip about the use of this word or expression in the context of native speakers of {targetLanguage}.
                - Keep your explanation in English.

                FORMATTING RULES:
                - Use strict Markdown with ## for main headers and ### for subheaders.
                - Use **bold** for key terms and *italics* for examples.
                - Use numbered and bulleted lists where appropriate.
                - Separate sections with blank lines for readability.
                - Use emojis in section headers as shown above.
                """;

        private static final String USER_PROMPT = """
                Target language: {targetLanguage}

                User word or phrase:
                {query}
                """;

        public Flux<String> generateTutorResponse(String query, String targetLanguage) {
                log.info("Language Tutor query: '{}' | Target language: {}", query, targetLanguage);

                PromptTemplate systemTemplate = new PromptTemplate(TUTOR_SYSTEM_PROMPT);
                String systemMessage = systemTemplate.render(Map.of("targetLanguage", targetLanguage));

                PromptTemplate userTemplate = new PromptTemplate(USER_PROMPT);
                String userMessage = userTemplate.render(Map.of(
                                "targetLanguage", targetLanguage,
                                "query", query));

                // Build the prompt with system + user messages
                Prompt prompt = new Prompt(
                                new org.springframework.ai.chat.messages.SystemMessage(systemMessage),
                                new org.springframework.ai.chat.messages.UserMessage(userMessage));

                ChatClient chatClient = chatClientBuilder.build();
                return chatClient.prompt(prompt)
                                .stream()
                                .content()
                                .filter(chunk -> chunk != null);
        }
}
