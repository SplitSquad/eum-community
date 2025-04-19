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
    String isState;
    String title;
    String content;
    String userName;
    String createdAt;
    String category;
    List<String> files;
    List<String> tags;

    @Builder
    public PostResDto(Long postId, Long views, Long like, Long dislike, String isState, String title, String content, String userName, String createdAt, String category, List<String> files, List<String> tags) {
        this.postId = postId;
        this.views = views;
        this.like = like;
        this.dislike = dislike;
        this.isState = isState;
        this.title = title;
        this.content = content;
        this.userName = userName;
        this.createdAt = createdAt;
        this.category = category;
        this.files = files;
        this.tags = tags;
    }
}
