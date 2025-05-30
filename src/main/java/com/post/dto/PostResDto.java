package com.post.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PostResDto {
    Long postId;
    Long views;
    Long like;
    Long dislike;
    Long commentCnt;
    Long userId;
    String isState;
    String title;
    String content;
    String userName;
    String createdAt;
    String category;
    String postType;
    String address;
    String nation;
    Integer origin;
    List<String> files;
    List<String> tags;

    @Builder
    public PostResDto(Long postId, Long userId, Long views, Long like, Long dislike, Long commentCnt, String isState, String title, String content, String userName, String createdAt, String category, String postType, String address, List<String> files, List<String> tags, String nation, Integer origin) {
        this.postId = postId;
        this.userId = userId;
        this.views = views;
        this.like = like;
        this.dislike = dislike;
        this.commentCnt = commentCnt;
        this.isState = isState;
        this.title = title;
        this.content = content;
        this.userName = userName;
        this.createdAt = createdAt;
        this.category = category;
        this.postType = postType;
        this.address = address;
        this.files = files;
        this.tags = tags;
        this.nation = nation;
        this.origin = origin;
    }
}
