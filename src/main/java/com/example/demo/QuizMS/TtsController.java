package com.example.demo.QuizMS;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    @Value("${elevenlabs.api.key}")
    private String apiKey;

    @Value("${elevenlabs.voice.id:pNInz6obpgDQGcFmaJgB}")
    private String voiceId;

    private static final String ELEVENLABS_URL =
            "https://api.elevenlabs.io/v1/text-to-speech/";

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    @PostMapping("/speak")
    public ResponseEntity<byte[]> speak(@RequestBody Map<String, String> body) {

        String text = body.get("text");

        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (text.length() > 500) {
            text = text.substring(0, 500);
        }

        // ✅ Corps JSON propre (Spring le sérialise automatiquement)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("model_id", "eleven_multilingual_v2");

        Map<String, Object> voiceSettings = new HashMap<>();
        voiceSettings.put("stability", 0.5);
        voiceSettings.put("similarity_boost", 0.75);

        requestBody.put("voice_settings", voiceSettings);

        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("audio/mpeg"));

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<byte[]> response =
                    buildRestTemplate().exchange(
                            ELEVENLABS_URL + voiceId,
                            HttpMethod.POST,
                            entity,
                            byte[].class
                    );

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {

            System.err.println("❌ ElevenLabs error: "
                    + e.getStatusCode()
                    + " — "
                    + e.getResponseBodyAsString());

            return ResponseEntity.status(e.getStatusCode()).build();

        } catch (Exception e) {

            System.err.println("❌ Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/voices")
    public ResponseEntity<String> getVoices() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", apiKey);

        try {
            ResponseEntity<String> response =
                    buildRestTemplate().exchange(
                            "https://api.elevenlabs.io/v1/voices",
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            String.class
                    );

            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {

            System.err.println("❌ Voices error: "
                    + e.getStatusCode()
                    + " — "
                    + e.getResponseBodyAsString());

            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }
}