package com.post.dto;

import lombok.Data;

@Data
public class ReplyReqDto {
    String content;
    String language;
    String emotion;
    Long commentId;
}
