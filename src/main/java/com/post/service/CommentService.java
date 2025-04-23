package com.post.service;

import com.post.dto.CommentReqDto;
import com.post.dto.CommentResDto;
import com.post.dto.KafkaCommentDto;
import com.post.entity.*;
import com.post.repository.*;
import util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TranslatedCommentRepository translatedCommentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final TranslatedPostRepository translatedPostRepository;

    private final JwtUtil jwtUtil;
    private final TranslationService translationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ResponseEntity<?> addComment(String token, CommentReqDto commentReqDto) throws JsonProcessingException {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Post post = postRepository.findById(commentReqDto.getPostId()).get();

        Comment comment = Comment.builder()
                .post(post)
                .user(user.get())
                .replyCnt(0L)
                .heart(0L)
                .build();

        comment = commentRepository.save(comment);

        CommentResDto commentResDto = CommentResDto.builder()
                .commentId(comment.getCommentId())
                .content(commentReqDto.getContent())
                .like(0L)
                .dislike(0L)
                .createdAt(comment.getCreatedAt())
                .userName(comment.getUser().getName())
                .reply(0L)
                .build();

        translationService.translateComment(comment, commentReqDto, null);

        KafkaCommentDto kafkaCommentDto = KafkaCommentDto.builder()
                .receiverId(post.getUser().getUserId())
                .senderId(user.get().getUserId())
                .build();

        kafkaTemplate.send("commentToPost", objectMapper.writeValueAsString(kafkaCommentDto));
        System.out.println("카프카 전송 완료");

        return ResponseEntity.ok(commentResDto);
    }

    public ResponseEntity<?> getComments(String token, long postId, String sort, int page, int size) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        String language = user.get().getLanguage();

        Sort sortOptions; // 정렬
        switch (sort) {
            case "latest":
                sortOptions = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "oldest":
                sortOptions = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "heart":
                sortOptions = Sort.by(Sort.Direction.DESC, "heart");
                break;
            default:
                sortOptions = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }
        Pageable pageable = PageRequest.of(page,size,sortOptions);
        Page<Comment> commentList = commentRepository.findByPost_PostId(postId, pageable);

        long total = commentList.getTotalElements();

        List<CommentResDto> commentResDtoList = new ArrayList<>();
        for(Comment comment : commentList){
            TranslatedComment translatedComment = translatedCommentRepository
                    .findByComment_CommentIdAndLanguage(comment.getCommentId(), language);

            if(translatedComment == null) continue;

            long dislike = commentReactionRepository
                    .countByComment_CommentIdAndOption(comment.getCommentId(), "싫어요");

            CommentReaction commentReaction = commentReactionRepository
                    .findByComment_CommentIdAndUser_UserId(comment.getCommentId(), user.get().getUserId());
            String option = null;
            if(commentReaction != null) {
                option = commentReaction.getOption();
            }

            CommentResDto commentResDto = CommentResDto.builder()
                    .commentId(comment.getCommentId())
                    .content(translatedComment.getContent())
                    .like(comment.getHeart())
                    .dislike(dislike)
                    .reply(comment.getReplyCnt())
                    .createdAt(comment.getCreatedAt())
                    .userName(comment.getUser().getName())
                    .isState(option)
                    .build();

            commentResDtoList.add(commentResDto);
        }
        return ResponseEntity.ok(Map.of(
                "commentList", commentResDtoList,
                "total", total
        ));
    }

    public ResponseEntity<?> updateComment(String token, long commentId, CommentReqDto commentReqDto) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Comment comment = commentRepository.findById(commentId).get();

        if(user.get() != comment.getUser()) {
            return ResponseEntity.badRequest().body("작성자만 수정 가능");
        }

        translationService.translateComment(comment, commentReqDto, commentId);

        return ResponseEntity.ok(commentReqDto.getContent());
    }

    @Transactional
    public ResponseEntity<?> deleteComment(String token, long commentId) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Comment comment = commentRepository.findById(commentId).get();

        if(user.get() != comment.getUser() && !user.get().getRole().equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body("작성자/관리자만 수정 가능");
        }
        commentRepository.delete(comment);
        return ResponseEntity.ok("삭제 완료");
    }

    @Transactional
    public ResponseEntity<?> reactToComment(String token, long commentId, CommentReqDto commentReqDto) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        long userId = user.get().getUserId();
        Comment comment = commentRepository.findById(commentId).get();

        CommentReaction commentReaction = commentReactionRepository
                .findByComment_CommentIdAndUser_UserId(commentId, userId);

        if(commentReaction == null){
            commentReaction = CommentReaction.builder()
                    .comment(comment)
                    .user(user.get())
                    .option(commentReqDto.getEmotion())
                    .build();

            if(commentReqDto.getEmotion().equals("좋아요")){
                comment.setHeart(comment.getHeart() + 1);
                commentRepository.save(comment);
            }
            commentReactionRepository.save(commentReaction);

            long like = comment.getHeart();
            long dislike = commentReactionRepository.countByComment_CommentIdAndOption(commentId, "싫어요");

            return ResponseEntity.ok(Map.of(
                    "like", like,
                    "dislike", dislike
            ));
        }
        else{
            if(commentReaction.getOption().equals(commentReqDto.getEmotion())) {
                if(commentReqDto.getEmotion().equals("좋아요")){
                    comment.setHeart(comment.getHeart() - 1);
                    commentRepository.save(comment);
                }

                commentReactionRepository.delete(commentReaction);

                long like = comment.getHeart();
                long dislike = commentReactionRepository.countByComment_CommentIdAndOption(commentId, "싫어요");
                return ResponseEntity.ok(Map.of(
                        "like", like,
                        "dislike", dislike
                ));
            }
            return ResponseEntity.ok("좋아요와 싫어요는 동시에 등록 불가");
        }
    }

    public ResponseEntity<?> getMyComment(String token, long userId, int page, int size) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        String language = user.get().getLanguage();

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentList = commentRepository.findByUser_UserId(userId, pageable);

        long total = commentList.getTotalElements();

        List<CommentResDto> commentResDtoList = new ArrayList<>();

        for (Comment comment : commentList) {
            TranslatedComment translatedComment = translatedCommentRepository
                    .findByComment_CommentIdAndLanguage(comment.getCommentId(), language);

            TranslatedPost translatedPost = translatedPostRepository
                    .findByPost_PostIdAndLanguage(comment.getPost().getPostId(), language);

            CommentReaction commentReaction = commentReactionRepository
                    .findByComment_CommentIdAndUser_UserId(comment.getCommentId(), user.get().getUserId());
            String option = null;
            if(commentReaction != null) {
                option = commentReaction.getOption();
            }

            CommentResDto commentResDto = CommentResDto.builder()
                    .commentId(comment.getCommentId())
                    .createdAt(comment.getCreatedAt())
                    .content(translatedComment.getContent())
                    .postTitle(translatedPost.getTitle())
                    .userName(comment.getUser().getName())
                    .postId(comment.getPost().getPostId())
                    .isState(option)
                    .build();

            commentResDtoList.add(commentResDto);
        }
        return ResponseEntity.ok(Map.of(
                "commentList", commentResDtoList
                ,"total", total
        ));
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
