package com.example.post.service;

import com.example.post.dto.KafkaCommentDto;
import com.example.post.dto.ReplyReqDto;
import com.example.post.dto.ReplyResDto;
import com.example.post.entity.*;
import com.example.post.repository.*;
import com.example.post.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyService {
    private final JwtUtil jwtUtil;
    private final TranslationService translationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final TranslatedReplyRepository translatedReplyRepository;
    private final ReplyReactionRepository replyReactionRepository;

    private final String[] targetLanguage = {"KO", "EN", "JA", "ZH", "DE", "FR", "ES", "RU"};

    public ResponseEntity<?> addReply(String token, ReplyReqDto replyReqDto) throws JsonProcessingException {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Comment comment = commentRepository.findById(replyReqDto.getCommentId()).orElse(null);
        Reply reply = new Reply();
        reply.setComment(comment);
        reply.setUser(user.get());
        reply = replyRepository.save(reply);

        ReplyResDto replyResDto = new ReplyResDto();
        replyResDto.setContent(replyReqDto.getContent());
        replyResDto.setLike(0L);
        replyResDto.setDislike(0L);
        replyResDto.setReplyId(reply.getReplyId());
        replyResDto.setUserName(reply.getUser().getName());
        replyResDto.setCreatedAt(reply.getCreatedAt());

        for(int i = 0; i < targetLanguage.length; i++) { // 9개 언어로 번역해서 저장
            TranslatedReply translatedReply = new TranslatedReply();
            translatedReply.setLanguage(targetLanguage[i]);
            translatedReply.setReply(reply);

            if(replyReqDto.getLanguage().equals(targetLanguage[i])) {
                translatedReply.setContent(replyReqDto.getContent());
                translatedReplyRepository.save(translatedReply);
                continue;
            }

            Optional<String> translatedContent = translationService.translate(
                    replyReqDto.getContent(), replyReqDto.getLanguage(), targetLanguage[i]);


            if (translatedContent.isEmpty()) continue;

            translatedReply.setContent(translatedContent.get());
            translatedReplyRepository.save(translatedReply);
        }

        comment.setReplyCnt(comment.getReplyCnt() + 1);
        commentRepository.save(comment);

        KafkaCommentDto kafkaCommentDto = new KafkaCommentDto();
        kafkaCommentDto.setReceiverId(comment.getUser().getUserId());
        kafkaCommentDto.setSenderId(user.get().getUserId());

        kafkaTemplate.send("replyToComment", objectMapper.writeValueAsString(kafkaCommentDto));
        System.out.println("카프카 전송 완료");

        kafkaCommentDto = new KafkaCommentDto();
        kafkaCommentDto.setReceiverId(comment.getPost().getUser().getUserId());
        kafkaCommentDto.setSenderId(user.get().getUserId());

        kafkaTemplate.send("commentToPost", objectMapper.writeValueAsString(kafkaCommentDto));
        System.out.println("카프카 전송 완료");

        return ResponseEntity.ok(replyResDto);
    }

    public ResponseEntity<?> getReply(String token, long commentId) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        String language = user.get().getLanguage();

        List<Reply> replyList = replyRepository.findByComment_CommentId(commentId);

        List<ReplyResDto> replyResDtoList = new ArrayList<>();
        for(Reply reply : replyList) {
            TranslatedReply translatedReply = translatedReplyRepository
                    .findByReply_ReplyIdAndLanguage(reply.getReplyId(), language);

            long like = replyReactionRepository.countByReply_ReplyIdAndOption(reply.getReplyId(), "좋아요");
            long dislike = replyReactionRepository.countByReply_ReplyIdAndOption(reply.getReplyId(), "싫어요");

            ReplyResDto replyResDto = new ReplyResDto();
            replyResDto.setContent(translatedReply.getContent());
            replyResDto.setLike(like);
            replyResDto.setDislike(dislike);
            replyResDto.setReplyId(reply.getReplyId());
            replyResDto.setUserName(reply.getUser().getName());
            replyResDto.setCreatedAt(reply.getCreatedAt());

            replyResDtoList.add(replyResDto);
        }
        return ResponseEntity.ok(replyResDtoList);
    }

    public ResponseEntity<?> updateReply(String token, long replyId, ReplyReqDto replyReqDto) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Reply reply = replyRepository.findByReplyId(replyId);

        if(reply.getUser() != user.get()){
            return ResponseEntity.badRequest().body("작성자만 수정 가능");
        }

        for(int i = 0; i < targetLanguage.length; i++) { // 9개 언어로 번역해서 저장
            TranslatedReply translatedReply = translatedReplyRepository
                    .findByReply_ReplyIdAndLanguage(reply.getReplyId(), targetLanguage[i]);

            if(replyReqDto.getLanguage().equals(targetLanguage[i])) {
                translatedReply.setContent(replyReqDto.getContent());
                translatedReplyRepository.save(translatedReply);
                continue;
            }

            Optional<String> translatedContent = translationService.translate(
                    replyReqDto.getContent(), replyReqDto.getLanguage(), targetLanguage[i]);

            if (translatedContent.isEmpty()) continue;

            translatedReply.setContent(translatedContent.get());
            translatedReplyRepository.save(translatedReply);
        }

        return ResponseEntity.ok(replyReqDto.getContent());

    }

    @Transactional
    public ResponseEntity<?> deleteReply(String token, long replyId) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Reply reply = replyRepository.findByReplyId(replyId);

        if(user.get() != reply.getUser() && !user.get().getRole().equals("ADMIN")) {
            return ResponseEntity.badRequest().body("작성자/관리자만 수정 가능");
        }

        Comment comment = reply.getComment();
        comment.setReplyCnt(comment.getReplyCnt() - 1);
        commentRepository.save(comment);
        replyRepository.delete(reply);
        return ResponseEntity.ok("삭제 완료");
    }

    @Transactional
    public ResponseEntity<?> reactToReply(String token, long replyId, ReplyReqDto replyReqDto) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        long userId = user.get().getUserId();
        Reply reply = replyRepository.findByReplyId(replyId);

        ReplyReaction replyReaction = replyReactionRepository
                .findByReply_ReplyIdAndUser_UserId(replyId, userId);

        if(replyReaction == null){
            replyReaction = new ReplyReaction();
            replyReaction.setReply(reply);
            replyReaction.setUser(user.get());
            replyReaction.setOption(replyReqDto.getEmotion());

            replyReactionRepository.save(replyReaction);

            long like = replyReactionRepository.countByReply_ReplyIdAndOption(replyId, "좋아요");
            long dislike = replyReactionRepository.countByReply_ReplyIdAndOption(replyId, "싫어요");

            return ResponseEntity.ok(Map.of(
                    "like", like,
                    "dislike", dislike
            ));
        }
        else{
            if(replyReaction.getOption().equals(replyReqDto.getEmotion())) {

                replyReactionRepository.delete(replyReaction);

                long like = replyReactionRepository.countByReply_ReplyIdAndOption(replyId, "좋아요");
                long dislike = replyReactionRepository.countByReply_ReplyIdAndOption(replyId, "싫어요");
                return ResponseEntity.ok(Map.of(
                        "like", like,
                        "dislike", dislike
                ));
            }
            return ResponseEntity.ok("좋아요와 싫어요는 동시에 등록 불가");
        }
    }

    private Optional<User> verifyToken(String token) {    // 토큰 검증 함수
        try {
            long userId = jwtUtil.getUserId(token);
            User user = userRepository.findById(userId).orElse(null);
            if(user == null) {
                return Optional.empty();
            }
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
