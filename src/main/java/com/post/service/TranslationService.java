package com.post.service;

import com.post.dto.CommentReqDto;
import com.post.dto.PostReqDto;
import com.post.dto.ReplyReqDto;
import com.post.entity.*;
import com.post.repository.TranslatedCommentRepository;
import com.post.repository.TranslatedPostRepository;
import com.post.repository.TranslatedReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslationService {
    @Value("${translation.api-key}")
    private String apiKey;
    private final TranslatedPostRepository translatedPostRepository;
    private final TranslatedCommentRepository translatedCommentRepository;
    private final TranslatedReplyRepository translatedReplyRepository;

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
    public void translatePost(Post post, PostReqDto postReqDto, Long postId) {
        for(int i = 0; i < targetLanguage.length; i++) { // 9개 언어로 번역해서 저장
            TranslatedPost translatedPost;
            if(postId == null){
                translatedPost = new TranslatedPost();
            }
            else{
                translatedPost = translatedPostRepository.findByPost_PostIdAndLanguage(postId, targetLanguage[i]);
            }
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

    @Async
    public void translateComment(Comment comment, CommentReqDto commentReqDto, Long commentId) {
        for(int i = 0; i < targetLanguage.length; i++) { // 9개 언어로 번역해서 저장
            TranslatedComment translatedComment;
            if(commentId == null){
                translatedComment = new TranslatedComment();
            }
            else{
                translatedComment = translatedCommentRepository.
                        findByComment_CommentIdAndLanguage(comment.getCommentId(), targetLanguage[i]);
            }
            translatedComment.setComment(comment);
            translatedComment.setLanguage(targetLanguage[i]);

            if(commentReqDto.getLanguage().equals(targetLanguage[i])) {
                translatedComment.setContent(commentReqDto.getContent());
                translatedCommentRepository.save(translatedComment);
                continue;
            }

            Optional<String> translatedContent = translate(
                    commentReqDto.getContent(), commentReqDto.getLanguage(), targetLanguage[i]);


            if (translatedContent.isEmpty()) continue;

            translatedComment.setContent(translatedContent.get());
            translatedCommentRepository.save(translatedComment);
        }
    }

    @Async
    public void translateReply(Reply reply, ReplyReqDto replyReqDto, Long replyId) {
        for(int i = 0; i < targetLanguage.length; i++) { // 9개 언어로 번역해서 저장
            TranslatedReply translatedReply;
            if(replyId == null){
                translatedReply = new TranslatedReply();
            }
            else{
                translatedReply = translatedReplyRepository
                        .findByReply_ReplyIdAndLanguage(replyId, targetLanguage[i]);
            }
            translatedReply.setLanguage(targetLanguage[i]);
            translatedReply.setReply(reply);

            if(replyReqDto.getLanguage().equals(targetLanguage[i])) {
                translatedReply.setContent(replyReqDto.getContent());
                translatedReplyRepository.save(translatedReply);
                continue;
            }

            Optional<String> translatedContent = translate(
                    replyReqDto.getContent(), replyReqDto.getLanguage(), targetLanguage[i]);

            if (translatedContent.isEmpty()) continue;

            translatedReply.setContent(translatedContent.get());
            translatedReplyRepository.save(translatedReply);
        }
    }
}