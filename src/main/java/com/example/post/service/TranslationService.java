package com.example.post.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TranslationService {
    @Value("${translation.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api-free.deepl.com/v2/translate";

    public Optional<String> translate(String text, String sourceLang, String targetLang) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("auth_key", apiKey);
        body.add("text", text);
        body.add("source_lang", sourceLang.toUpperCase()); // ex: KO
        body.add("target_lang", targetLang.toUpperCase()); // ex: EN

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);
            Map<String, Object> result = response.getBody();

            List<Map<String, String>> translations = (List<Map<String, String>>) result.get("translations");
            return Optional.of(translations.get(0).get("text"));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}