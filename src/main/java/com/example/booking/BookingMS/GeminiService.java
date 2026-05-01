package com.example.booking.BookingMS;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    public String getRecommendation(String prompt) {
        String url = "/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "maxOutputTokens", 8192
                )
        );

        Map<?, ?> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<?> candidates = (List<?>) response.get("candidates");
        Map<?, ?> first    = (Map<?, ?>) candidates.get(0);
        Map<?, ?> content  = (Map<?, ?>) first.get("content");
        List<?> parts      = (List<?>) content.get("parts");
        Map<?, ?> part     = (Map<?, ?>) parts.get(0);

        return (String) part.get("text");
    }
}
