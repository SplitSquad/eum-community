package com.example.post.dto;

import lombok.Data;

@Data
public class CommentReqDto {
    String content;
    String language;
    String emotion;
    Long postId;
}
