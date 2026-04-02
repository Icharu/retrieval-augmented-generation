package com.example.ragsystem.controller;

import com.example.ragsystem.service.AnythingLLMClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkspaceController {

    private final AnythingLLMClient anythingLLMClient;

    @PostMapping("/{slug}/chat")
    public ResponseEntity<String> sendWorkspaceChat(@PathVariable String slug,
            @RequestBody WorkspaceChatRequest request) {
        try {
            String response = anythingLLMClient.sendWorkspaceChat(slug, request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error communicating with workspace: " + e.getMessage());
        }
    }

    @Data
    public static class WorkspaceChatRequest {
        private String message;
    }
}
