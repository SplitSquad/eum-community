package com.example.post.dto;

import lombok.Data;

@Data
public class CommentResDto {
    Long commentId;
    Long like;
    Long dislike;
    Long reply;
    Long postId;
    String content;
    String userName;
    String createdAt;
    String postTitle;
}
