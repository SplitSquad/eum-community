package com.example.post.service;

import com.example.post.dto.PostReqDto;
import com.example.post.entity.Post;
import com.example.post.entity.TranslatedPost;
import com.example.post.repository.TranslatedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
@RequiredArgsConstructor
public class TranslationService {
    @Value("${translation.api-key}")
    private String apiKey;
    private final TranslatedPostRepository translatedPostRepository;

    private static final String API_URL = "https://api-free.deepl.com/v2/translate";

    private final String[] targetLanguage = {"KO", "EN", "JA", "ZH", "DE", "FR", "ES", "RU"};

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

    @Async
    public void translatedAndSave(Post post, PostReqDto postReqDto){
        for(int i = 0; i < targetLanguage.length; i++) { // 9개 언어로 번역해서 저장
            TranslatedPost translatedPost = new TranslatedPost();
            translatedPost.setPost(post);
            translatedPost.setLanguage(targetLanguage[i]);

            if(postReqDto.getLanguage().equals(targetLanguage[i])) {
                translatedPost.setContent(postReqDto.getContent());
                translatedPost.setTitle(postReqDto.getTitle());
                translatedPostRepository.save(translatedPost);
                continue;
            }

            Optional<String> translatedTitle = translate(
                    postReqDto.getTitle(), postReqDto.getLanguage(), targetLanguage[i]);

            Optional<String> translatedContent = translate(
                    postReqDto.getContent(), postReqDto.getLanguage(), targetLanguage[i]);

            if (translatedTitle.isEmpty() || translatedContent.isEmpty()) continue;

            translatedPost.setContent(translatedContent.get());
            translatedPost.setTitle(translatedTitle.get());
            translatedPostRepository.save(translatedPost);
        }
    }
}