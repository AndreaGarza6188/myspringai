package com.example.myspringai.controller;

import com.example.myspringai.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller that exposes the AI agent to web clients.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/session} — create a new chat session</li>
 *   <li>{@code POST /api/chat}    — send a message and receive a response</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Create a new session and return its ID.
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> createSession() {
        String sessionId = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    /**
     * Send a chat message.
     *
     * <p>Request body (JSON):
     * <pre>{@code
     * {
     *   "sessionId": "...",   // optional; a new one is created if absent
     *   "message": "..."      // required
     * }
     * }</pre>
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message must not be empty"));
        }
        String sessionId = body.getOrDefault("sessionId", UUID.randomUUID().toString());

        try {
            String response = agentService.chat(sessionId, message);
            return ResponseEntity.ok(Map.of(
                    "response", response,
                    "sessionId", sessionId));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal error";
            return ResponseEntity.internalServerError().body(Map.of("error", msg));
        }
    }
}
