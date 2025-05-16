package com.post.service;

import com.post.dto.KafkaCommentDto;
import com.post.dto.ReplyReqDto;
import com.post.dto.ReplyResDto;
import com.post.entity.*;
import com.post.repository.*;
import util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import util.TranslationJob;
import util.TranslationQueue;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReplyService {
    private final JwtUtil jwtUtil;
    private final TranslationQueue translationQueue;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final TranslatedReplyRepository translatedReplyRepository;
    private final ReplyReactionRepository replyReactionRepository;

    public ResponseEntity<?> addReply(String token, ReplyReqDto replyReqDto) throws JsonProcessingException {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Comment comment = commentRepository.findById(replyReqDto.getCommentId()).orElse(null);
        Reply reply = Reply.builder()
                .comment(comment)
                .user(user.get())
                .build();
        reply = replyRepository.save(reply);

        ReplyResDto replyResDto = ReplyResDto.builder()
                .content(replyReqDto.getContent())
                .like(0L)
                .dislike(0L)
                .replyId(reply.getReplyId())
                .userName(reply.getUser().getName())
                .createdAt(reply.getCreatedAt())
                .build();

        translationQueue.enqueue(new TranslationJob(reply, replyReqDto, null));

        comment.setReplyCnt(comment.getReplyCnt() + 1);
        commentRepository.save(comment);

        if(!Objects.equals(comment.getUser().getUserId(), user.get().getUserId())) {
            KafkaCommentDto kafkaCommentDto = KafkaCommentDto.builder()
                    .receiverId(comment.getUser().getUserId())
                    .senderId(user.get().getUserId())
                    .build();
            kafkaTemplate.send("replyToComment", objectMapper.writeValueAsString(kafkaCommentDto));
        }

        if(!Objects.equals(comment.getPost().getUser().getUserId(), user.get().getUserId())) {
            KafkaCommentDto kafkaCommentDto = KafkaCommentDto.builder()
                    .receiverId(comment.getPost().getUser().getUserId())
                    .senderId(user.get().getUserId())
                    .build();

            kafkaTemplate.send("commentToPost", objectMapper.writeValueAsString(kafkaCommentDto));

        }

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

            if(translatedReply == null) continue;

            long like = replyReactionRepository.countByReply_ReplyIdAndOption(reply.getReplyId(), "좋아요");
            long dislike = replyReactionRepository.countByReply_ReplyIdAndOption(reply.getReplyId(), "싫어요");

            ReplyReaction replyReaction = replyReactionRepository
                    .findByReply_ReplyIdAndUser_UserId(reply.getReplyId(), user.get().getUserId());

            String option = null;
            if(replyReaction != null) {
                option = replyReaction.getOption();
            }

            ReplyResDto replyResDto = ReplyResDto.builder()
                    .replyId(reply.getReplyId())
                    .content(translatedReply.getContent())
                    .like(like)
                    .dislike(dislike)
                    .userName(reply.getUser().getName())
                    .createdAt(reply.getCreatedAt())
                    .isState(option)
                    .build();

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

        translationQueue.enqueue(new TranslationJob(reply, replyReqDto, replyId));

        return ResponseEntity.ok(replyReqDto.getContent());
    }

    @Transactional
    public ResponseEntity<?> deleteReply(String token, long replyId) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Reply reply = replyRepository.findByReplyId(replyId);

        if(user.get() != reply.getUser() && !user.get().getRole().equals("ROLE_ADMIN")) {
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
            replyReaction = ReplyReaction.builder()
                    .reply(reply)
                    .user(user.get())
                    .option(replyReqDto.getEmotion())
                    .build();

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
