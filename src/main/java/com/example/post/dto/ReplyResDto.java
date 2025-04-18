package com.example.post.dto;

import lombok.Data;

@Data
public class ReplyResDto {
    Long replyId;
    Long like;
    Long dislike;
    String isState;
    String content;
    String userName;
    String createdAt;
}
