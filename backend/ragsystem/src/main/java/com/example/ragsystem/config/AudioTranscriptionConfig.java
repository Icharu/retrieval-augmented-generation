package com.example.ragsystem.config;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AudioTranscriptionConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.audio.transcription.base-url:http://localhost:9000}")
    private String audioBaseUrl;

    @Value("${spring.ai.openai.audio.transcription.options.model:Systran/faster-whisper-small}")
    private String model;

    @Bean
    public OpenAiAudioApi openAiAudioApi() {
        return OpenAiAudioApi.builder()
                .baseUrl(audioBaseUrl)
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel(OpenAiAudioApi openAiAudioApi) {
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .model(model)
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();
        return new OpenAiAudioTranscriptionModel(openAiAudioApi, options);
    }
}