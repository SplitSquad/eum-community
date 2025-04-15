package com.example.post.controller;

import com.example.post.dto.PostReqDto;
import com.example.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/community/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestHeader("Authorization") String token, int page, int size,
                                      String category, String sort, String region) { // 게시글 목록 조회
        return postService.getPosts(token,page,size,category,sort,region);
        // region category 는 '전체' 로 설정할 시 전체글 조회
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestHeader("Authorization") String token,
                                   @RequestPart("post") PostReqDto postReqDto,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) { // 게시글 작성
        return postService.write(token, postReqDto, files);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@RequestHeader("Authorization") String token,
                                     @PathVariable("postId") long postId) { // 게시글 상세 조회
        return postService.getPost(token, postId);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> updatePost(@RequestHeader("Authorization") String token,
                                        @PathVariable Long postId,
                                        @RequestPart("post") PostReqDto postReqDto,
                                        @RequestPart(value = "files", required = false) List<MultipartFile> files){
        return postService.updatePost(token, postId, postReqDto, files);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@RequestHeader("Authorization") String token,
                                        @PathVariable Long postId){
        return postService.deletePost(token, postId);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(@RequestHeader("Authorization") String token,
                                         int page, int size, String category, String sort,
                                         String region, String keyword, String searchBy) {
        return postService.searchPosts(token, page, size, category, sort, region, keyword, searchBy);
    }

    @PostMapping("/emotion/{postId}")
    public ResponseEntity<?> reactToPost(@RequestHeader("Authorization") String token,
                                         @PathVariable Long postId,
                                         @RequestBody PostReqDto postReqDto){
        return postService.reactToPost(token, postId, postReqDto);
    }

    @GetMapping("/recommendation")
    public ResponseEntity<?> recommendPost(@RequestHeader("Authorization") String token,
                                      String tag, int cnt){
        return postService.recommendPost(token, tag, cnt);
    }

    @GetMapping("/written")
    public ResponseEntity<?> getMyPost(@RequestHeader("Authorization") String token,
            long userId, int page, int size){
        return postService.getMyPost(token, userId, page, size);
    }
}
