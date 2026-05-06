package com.example.booking.BookingMS;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, String>> recommend(
            @RequestBody Map<String, String> body) {

        String prompt = body.get("prompt");
        String result = geminiService.getRecommendation(prompt);

        return ResponseEntity.ok(Map.of("text", result));
    }
}