package com.example.ragsystem.controller;

import com.example.ragsystem.service.RAGService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final RAGService ragService;

    @PostMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> query(@RequestBody ChatRequest request) {
        return ragService.generateAnswer(request.getQuery());
    }

    @Data
    public static class ChatRequest {
        private String query;
    }

    @Data
    public static class ChatResponse {
        private final String answer;
    }
}
