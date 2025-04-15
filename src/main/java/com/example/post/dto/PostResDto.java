package com.example.post.dto;

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
    String title;
    String content;
    String userName;
    String createdAt;
    String category;
    List<String> files;
    List<String> tags;

    public PostResDto(Long postId, Long views, Long dislike,
                      Long like, String title, String content, String userName,
                      String createdAt, List<String> files,
                      String category, List<String> tags) {
        this.postId = postId;
        this.views = views;
        this.dislike = dislike;
        this.like = like;
        this.title = title;
        this.content = content;
        this.userName = userName;
        this.createdAt = createdAt;
        this.files = files;
        this.category = category;
        this.tags = tags;
    }
}
