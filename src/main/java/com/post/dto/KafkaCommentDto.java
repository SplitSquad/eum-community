package com.post.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KafkaCommentDto {
    Long receiverId;
    Long senderId;
    Long postId;
    String serviceType;

    @Builder
    public KafkaCommentDto(Long senderId, Long receiverId, Long postId, String serviceType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.postId = postId;
        this.serviceType = serviceType;
    }
}
