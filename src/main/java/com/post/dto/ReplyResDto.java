package com.post.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReplyResDto {
    Long replyId;
    Long like;
    Long dislike;
    Long userId;
    String isState;
    String content;
    String userName;
    String createdAt;

    @Builder
    public ReplyResDto(Long replyId, Long userId, String createdAt, String userName, String content, String isState, Long dislike, Long like) {
        this.replyId = replyId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.userName = userName;
        this.content = content;
        this.isState = isState;
        this.dislike = dislike;
        this.like = like;
    }
}
