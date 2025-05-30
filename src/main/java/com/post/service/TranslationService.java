package com.post.service;

import com.post.dto.CommentReqDto;
import com.post.dto.PostReqDto;
import com.post.dto.ReplyReqDto;
import com.post.entity.*;
import com.post.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import util.TranslationJob;

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

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    private static final String API_URL = "https://api-free.deepl.com/v2/translate";

    private final String[] targetLanguage = {"KO", "EN", "JA", "ZH", "DE", "FR", "ES", "RU"};

    public void handleJob(TranslationJob job) {
        if (job.getEntity() instanceof Post) {
            translatePost((Post) job.getEntity(), (PostReqDto) job.getDto(), job.getOptionalId());
        } else if (job.getEntity() instanceof Comment) {
            translateComment((Comment) job.getEntity(), (CommentReqDto) job.getDto(), job.getOptionalId());
        } else if (job.getEntity() instanceof Reply) {
            translateReply((Reply) job.getEntity(), (ReplyReqDto) job.getDto(), job.getOptionalId());
        }
    }

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

    public void translatePost(Post post, PostReqDto postReqDto, Long postId) {
        boolean flag = true;

        for (String language : targetLanguage) { // 9개 언어로 번역해서 저장
            TranslatedPost translatedPost = (postId == null) ? new TranslatedPost()
                    : translatedPostRepository.findByPost_PostIdAndLanguage(postId, language);

            translatedPost.setPost(post);
            translatedPost.setLanguage(language);

            if (postReqDto.getLanguage().toUpperCase().equals(language)) {
                translatedPost.setContent(postReqDto.getContent());
                translatedPost.setTitle(postReqDto.getTitle());
                translatedPost.setOrigin(1);
                translatedPostRepository.save(translatedPost);
                flag = false;
                continue;
            }

            Optional<String> translatedTitle = translate(
                    postReqDto.getTitle(), postReqDto.getLanguage(), language);

            Optional<String> translatedContent = translate(
                    postReqDto.getContent(), postReqDto.getLanguage(), language);

            if (translatedTitle.isEmpty() || translatedContent.isEmpty()) {
                kafkaTemplate.send("failPost", String.valueOf(post.getUser().getUserId()));
                postRepository.delete(post);
                break;
            }

            translatedPost.setContent(translatedContent.get());
            translatedPost.setTitle(translatedTitle.get());
            translatedPost.setOrigin(0);
            translatedPostRepository.save(translatedPost);
        }

        if (flag) { //원문 저장
            TranslatedPost translatedPost = (postId == null) ? new TranslatedPost()
                    : translatedPostRepository.findByPost_PostIdAndLanguage(postId, postReqDto.getLanguage());

            translatedPost.setPost(post);
            translatedPost.setLanguage(postReqDto.getLanguage());
            translatedPost.setContent(postReqDto.getContent());
            translatedPost.setTitle(postReqDto.getTitle());
            translatedPost.setOrigin(1);
            translatedPostRepository.save(translatedPost);
        }

    }

    public void translateComment(Comment comment, CommentReqDto commentReqDto, Long commentId) {
        for (String language : targetLanguage) { // 9개 언어로 번역해서 저장
            TranslatedComment translatedComment = (commentId == null) ? new TranslatedComment()
                    : translatedCommentRepository.findByComment_CommentIdAndLanguage(commentId, language);

            translatedComment.setComment(comment);
            translatedComment.setLanguage(language);

            if (commentReqDto.getLanguage().equals(language)) {
                translatedComment.setContent(commentReqDto.getContent());
                translatedCommentRepository.save(translatedComment);
                continue;
            }

            Optional<String> translatedContent = translate(
                    commentReqDto.getContent(), commentReqDto.getLanguage(), language);


            if (translatedContent.isEmpty()) {
                kafkaTemplate.send("failComment", String.valueOf(comment.getUser().getUserId()));
                commentRepository.delete(comment);
                break;
            }

            translatedComment.setContent(translatedContent.get());
            translatedCommentRepository.save(translatedComment);
        }
    }

    public void translateReply(Reply reply, ReplyReqDto replyReqDto, Long replyId) {
        for (String language : targetLanguage) { // 9개 언어로 번역해서 저장
            TranslatedReply translatedReply = (replyId == null) ? new TranslatedReply()
                    : translatedReplyRepository.findByReply_ReplyIdAndLanguage(replyId, language);

            translatedReply.setLanguage(language);
            translatedReply.setReply(reply);

            if (replyReqDto.getLanguage().equals(language)) {
                translatedReply.setContent(replyReqDto.getContent());
                translatedReplyRepository.save(translatedReply);
                continue;
            }

            Optional<String> translatedContent = translate(
                    replyReqDto.getContent(), replyReqDto.getLanguage(), language);

            if (translatedContent.isEmpty()){
                kafkaTemplate.send("failPost", String.valueOf(reply.getUser().getUserId()));
                replyRepository.delete(reply);
                break;
            }

            translatedReply.setContent(translatedContent.get());
            translatedReplyRepository.save(translatedReply);
        }
    }
}