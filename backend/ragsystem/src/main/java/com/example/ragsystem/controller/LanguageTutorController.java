package com.example.ragsystem.controller;

import com.example.ragsystem.service.LanguageTutorService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LanguageTutorController {

    private final LanguageTutorService tutorService;

    @PostMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> query(@RequestBody TutorRequest request) {
        return tutorService.generateTutorResponse(request.getQuery(), request.getTargetLanguage())
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> processAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetLanguage") String targetLanguage) {
        
        return tutorService.processAudioAndGenerateResponse(file, targetLanguage)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @Data
    public static class TutorRequest {
        private String query;
        private String targetLanguage;
    }
}
