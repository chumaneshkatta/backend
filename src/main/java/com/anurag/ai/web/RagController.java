package com.anurag.ai.web;

import com.anurag.ai.service.RagService;
import com.anurag.ai.web.Dto.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RagController {
    private final RagService rag;

    public RagController(RagService rag) { this.rag = rag; }

    @PostMapping("/rag/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) { return rag.answer(req.question()); }

    @Public
    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "ok"); }
}
