package com.post.service;

import com.post.dto.PostReqDto;
import com.post.dto.PostResDto;
import com.post.entity.*;
import com.post.repository.*;
import util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final TranslationService translationService;
    private final AwsS3Service awsS3Service;
    private final JwtUtil jwtUtil;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TranslatedPostRepository translatedPostRepository;
    private final PostFileRepository postFileRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final PostReactionRepository postReactionRepository;
    private final CommentRepository commentRepository;

    private Long getTotalComments(long postId){
        List<Comment> commentList = commentRepository.findByPost_PostId(postId);
        long total = commentList.size();
        for(Comment comment : commentList){
            total += comment.getReplyCnt();
        }
        return total;
    }

    private List<String> getTags(long postId){
        List<Tag> tagList = tagRepository.findTagsByPostId(postId);
        List<String> tags = new ArrayList<>();
        for(Tag tag : tagList){
            tags.add(tag.getName());
        }
        return tags;
    }

    private List<PostResDto> postListToDto(Page<Post> postList, String language){
        List<PostResDto> postResDtoList = new ArrayList<>();
        for(Post post : postList) {
            TranslatedPost translatedPost = //번역
                    translatedPostRepository.findByPost_PostIdAndLanguage(post.getPostId(), language);

            if(translatedPost == null) continue;

            long like = postReactionRepository.countByPost_postIdAndOption(post.getPostId(), "좋아요");
            long dislike = postReactionRepository.countByPost_postIdAndOption(post.getPostId(), "싫어요");

            long commentCnt = getTotalComments(post.getPostId());

            List<String> files = new ArrayList<>();
            List<PostFile> postFile = postFileRepository.findByPost_PostId(post.getPostId());
            if(!postFile.isEmpty()){
                files.add(postFile.get(0).getUrl());
            }

            List<String> tagList = getTags(post.getPostId());

            PostResDto postResDto = PostResDto.builder()
                    .postId(post.getPostId())
                    .title(translatedPost.getTitle())
                    .views(post.getViews())
                    .commentCnt(commentCnt)
                    .userName(post.getUser().getName())
                    .createdAt(post.getCreatedAt())
                    .like(like)
                    .dislike(dislike)
                    .postType(post.getPostType())
                    .address(post.getAddress())
                    .files(files)
                    .tags(tagList)
                    .category(post.getCategory())
                    .build();

            postResDtoList.add(postResDto);
        }
        return postResDtoList;
    }

    private List<PostResDto> translatedPostListToDto(Page<TranslatedPost> translatedPostList){
        List<PostResDto> postResDtoList = new ArrayList<>(); //dto 변환
        for(TranslatedPost translatedPost : translatedPostList) {

            long like = postReactionRepository.countByPost_postIdAndOption(translatedPost.getPost().getPostId(), "좋아요");
            long dislike = postReactionRepository.countByPost_postIdAndOption(translatedPost.getPost().getPostId(), "싫어요");

            long commentCnt = getTotalComments(translatedPost.getPost().getPostId());

            List<String> files = new ArrayList<>();
            List<PostFile> postFile = postFileRepository.findByPost_PostId(translatedPost.getPost().getPostId());
            if(!postFile.isEmpty()){
                files.add(postFile.get(0).getUrl());
            }

            List<String> tagList = getTags(translatedPost.getPost().getPostId());

            PostResDto postResDto = PostResDto.builder()
                    .postId(translatedPost.getPost().getPostId())
                    .title(translatedPost.getTitle())
                    .views(translatedPost.getPost().getViews())
                    .commentCnt(commentCnt)
                    .userName(translatedPost.getPost().getUser().getName())
                    .createdAt(translatedPost.getPost().getCreatedAt())
                    .like(like)
                    .dislike(dislike)
                    .postType(translatedPost.getPost().getPostType())
                    .address(translatedPost.getPost().getAddress())
                    .files(files)
                    .tags(tagList)
                    .category(translatedPost.getPost().getCategory())
                    .build();

            postResDtoList.add(postResDto);
        }
        return postResDtoList;
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

    public String extractKeyFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);  // 맨 마지막 파일명만 추출
    }


    public ResponseEntity<?> getPosts(String token, int page, int size, String category, String sort
            , String region, String postType, List<String> tags) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        String language = user.get().getLanguage();

        Sort sortOptions;
        switch (sort) {
            case "oldest":
                sortOptions = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "views":
                sortOptions = Sort.by(Sort.Direction.DESC, "views");
                break;
            default:
                sortOptions = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }
        Pageable pageable = PageRequest.of(page,size,sortOptions);

        Page<Post> postList;

        if(tags == null){
            postList = postRepository.findByCategoryAndAddressLikeAndPostType(category, region, postType, pageable);
        }
        else{
            postList = postRepository.findByCategoryAndAddressLikeAndPostTypeAndTagNamesIn
                    (category, region, postType, tags, pageable);
        }


        long total = postList.getTotalElements();

        List<PostResDto> postResDtoList = postListToDto(postList, language);//new ArrayList<>();

        return ResponseEntity.ok(Map.of(
                "postList", postResDtoList,
                "total", total
        ));
    }

    public ResponseEntity<?> write(String token, PostReqDto postReqDto, List<MultipartFile> files) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Post post = Post.builder()
                .postType(postReqDto.getPostType())
                .address(postReqDto.getAddress())
                .views(0L)
                .category(postReqDto.getCategory())
                .user(user.get())
                .build();

        List<String> urls = new ArrayList<>();

        if(files == null){
            post.setIsFile(0);
            post = postRepository.save(post);
        }
        else{ // 파일 저장
            post.setIsFile(1);
            post = postRepository.save(post);

            for(MultipartFile file : files){
                try{
                    String url = awsS3Service.upload(file);

                    PostFile postFile = PostFile.builder()
                            .post(post)
                            .url(url)
                            .build();
                    postFileRepository.save(postFile);

                    urls.add(url);
                }catch(Exception e){
                    postRepository.delete(post);
                    return ResponseEntity.badRequest().body("잘못된 파일 형식");
                }
            }
        }

        for(String tagName : postReqDto.getTags()) {
            Tag tag = tagRepository.findByName(tagName);
            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();
            postTagRepository.save(postTag);
        }

        translationService.translatePost(post, postReqDto, null);

        PostResDto postResDto = PostResDto.builder()
                .postId(post.getPostId())
                .like(0L)
                .dislike(0L)
                .views(0L)
                .commentCnt(0L)
                .userName(post.getUser().getName())
                .title(postReqDto.getTitle())
                .content(postReqDto.getContent())
                .createdAt(post.getCreatedAt())
                .files(urls)
                .category(post.getCategory())
                .tags(postReqDto.getTags())
                .postType(postReqDto.getPostType())
                .address(postReqDto.getAddress())
                .build();

        return ResponseEntity.ok(postResDto);
    }

    public ResponseEntity<?> getPost(String token, long postId) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        String language = user.get().getLanguage();

        Post post = postRepository.findById(postId).orElse(null);
        if(post == null) {
            return ResponseEntity.badRequest().body("잘못된 게시글");
        }

        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        List<PostFile> postFileList = postFileRepository.findByPost_postId(postId);
        TranslatedPost translatedPost = translatedPostRepository.findByPost_PostIdAndLanguage(
                postId, language);
        long like = postReactionRepository.countByPost_postIdAndOption(postId, "좋아요");
        long dislike = postReactionRepository.countByPost_postIdAndOption(postId, "싫어요");

        List<String> urls = new ArrayList<>();
        for(PostFile postFile : postFileList){
            urls.add(postFile.getUrl());
        }

        List<String> tags = getTags(postId);

        PostReaction postReaction = postReactionRepository.findByUser_UserIdAndPost_PostId(
                user.get().getUserId(), postId
        );
        String option = null;
        if(postReaction != null) {
            option = postReaction.getOption();
        }

        long commentCnt = getTotalComments(postId);

        PostResDto postResDto = PostResDto.builder()
                .postId(post.getPostId())
                .like(like)
                .dislike(dislike)
                .views(post.getViews())
                .commentCnt(commentCnt)
                .userName(post.getUser().getName())
                .title(translatedPost.getTitle())
                .content(translatedPost.getContent())
                .createdAt(post.getCreatedAt())
                .files(urls)
                .category(post.getCategory())
                .tags(tags)
                .isState(option)
                .postType(post.getPostType())
                .address(post.getAddress())
                .build();

        return ResponseEntity.ok(postResDto);
    }

    @Transactional
    public ResponseEntity<?> updatePost(String token, Long postId, PostReqDto postReqDto, List<MultipartFile> files) { //게시글 수정 시 s3 어쩌구
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if(post == null) {
            return ResponseEntity.badRequest().body("잘못된 게시글");
        }

        List<PostFile> postFileList = postFileRepository.findByPost_PostId(postId);
        for(PostFile file : postFileList) {
            String key = extractKeyFromUrl(file.getUrl());
            awsS3Service.delete(key);
        }

        postFileRepository.deleteByPost_PostId(postId);


        List<String> urls = new ArrayList<>();
        if(files == null){
            post.setIsFile(0);
            postRepository.save(post);
        }
        else{
            post.setIsFile(1);
            postRepository.save(post);
            for(MultipartFile file : files){
                try{
                    String url = awsS3Service.upload(file);

                    PostFile postFile = PostFile.builder()
                            .post(post)
                            .url(url)
                            .build();
                    postFileRepository.save(postFile);

                    urls.add(url);
                }catch(Exception e){
                    postRepository.delete(post);
                    return ResponseEntity.badRequest().body("잘못된 파일 형식");
                }
            }
        }

        translationService.translatePost(post, postReqDto, postId);

        PostResDto postResDto = PostResDto.builder()
                .title(postReqDto.getTitle())
                .content(postReqDto.getContent())
                .files(urls)
                .build();

        return ResponseEntity.ok(postResDto);
    }

    @Transactional
    public ResponseEntity<?> deletePost(String token, Long postId) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        Post post = postRepository.findById(postId).orElse(null);

        if(post == null){
            return ResponseEntity.badRequest().body("존재하지 않는 게시글");
        }

        if(!user.get().getRole().equals("ROLE_ADMIN") && user.get().getUserId() !=
            post.getUser().getUserId()) {
            return ResponseEntity.badRequest().body("작성자/관리자만 삭제 가능");
        }

        if(post.getIsFile() == 1) {
            List<PostFile> postFileList = postFileRepository.findByPost_PostId(postId);
            for(PostFile postFile : postFileList){
                String url = postFile.getUrl();
                String key = extractKeyFromUrl(url);
                awsS3Service.delete(key);
                postFileRepository.delete(postFile);
            }
        }
        postRepository.deleteById(postId);
        return ResponseEntity.ok().body("삭제 완료");
    }

    public ResponseEntity<?> searchPosts(String token, int page, int size, String category,
                                         String sort, String region, String keyword, String searchBy, String postType) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        String language = user.get().getLanguage();

        Sort sortOptions; // 정렬
        switch (sort) {
            case "latest":
                sortOptions = Sort.by(Sort.Direction.DESC, "post.createdAt");
                break;
            case "oldest":
                sortOptions = Sort.by(Sort.Direction.ASC, "post.createdAt");
                break;
            case "views":
                sortOptions = Sort.by(Sort.Direction.DESC, "post.views");
                break;
            default:
                sortOptions = Sort.by(Sort.Direction.DESC, "post.createdAt");
                break;
        }

        Pageable pageable = PageRequest.of(page,size,sortOptions);
        Page<TranslatedPost> postList;
        System.out.println(keyword + " " + postType);
        if(searchBy.equals("제목")) {
            postList = translatedPostRepository.findByCategoryAndRegionAndPostTypeAndTitle(
                    category, region, keyword, language, postType, pageable);
        }else if (searchBy.equals("내용")) {
            postList = translatedPostRepository.findByCategoryAndRegionAndPostTypeAndContent(
                    category, region, keyword, language, postType, pageable);
        } else if (searchBy.equals("제목_내용")) {
            postList = translatedPostRepository.findByCategoryAndRegionAndPostTypeAndTitleOrContent(
                    category, region, keyword, language, postType, pageable);
        } else { // 작성자
            postList = translatedPostRepository.findByCategoryAndRegionAndPostTypeAndUsername(
                    category, region, keyword, language, postType, pageable);
        }

        long total = postList.getTotalElements();

        List<PostResDto> postResDtoList = translatedPostListToDto(postList);

        return ResponseEntity.ok(Map.of(
                "postList", postResDtoList,
                "total", total
        ));
    }

    @Transactional
    public ResponseEntity<?> reactToPost(String token, Long postId, PostReqDto postReqDto) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        long userId = user.get().getUserId();

        PostReaction postReaction = postReactionRepository.findByUser_UserIdAndPost_PostId(userId, postId);

        if(postReaction == null) {
            postReaction = PostReaction.builder()
                    .post(postRepository.findById(postId).get())
                    .user(user.get())
                    .option(postReqDto.getEmotion())
                    .build();
            postReactionRepository.save(postReaction);

            long like = postReactionRepository.countByPost_postIdAndOption(postId, "좋아요");
            long dislike = postReactionRepository.countByPost_postIdAndOption(postId, "싫어요");

            return ResponseEntity.ok(Map.of(
                    "like", like,
                    "dislike", dislike
            ));
        }
        else{
            if(postReaction.getOption().equals(postReqDto.getEmotion())){
                postReactionRepository.delete(postReaction);

                long like = postReactionRepository.countByPost_postIdAndOption(postId, "좋아요");
                long dislike = postReactionRepository.countByPost_postIdAndOption(postId, "싫어요");

                return ResponseEntity.ok(Map.of(
                        "like", like,
                        "dislike", dislike
                ));
            }
            else{
                return ResponseEntity.ok("좋아요와 싫어요는 동시에 등록 불가");
            }
        }
    }

    public ResponseEntity<?> recommendPost(String token, String tag, int cnt) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        String language = user.get().getLanguage();

        Pageable pageable = PageRequest.of(0, cnt);
        String sevenDaysAgo = LocalDateTime.now().minusDays(7).toString();

        Page<TranslatedPost> postList = translatedPostRepository.findTopByTagAndLanguageAndRecentDate(
                tag, language, sevenDaysAgo, pageable);

        List<PostResDto> postResDtoList = this.translatedPostListToDto(postList);

        return ResponseEntity.ok(postResDtoList);
    }

    public ResponseEntity<?> getMyPost(String token, long userId, int page, int size) {
        Optional<User> user = verifyToken(token);
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }

        String language = user.get().getLanguage();

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postList = postRepository.findByUser_UserId(userId, pageable);

        long total = postList.getTotalElements();

        List<PostResDto> postResDtoList = postListToDto(postList, language);

        return ResponseEntity.ok(Map.of(
                "postList", postResDtoList,
                "total", total
        ));
    }
}
