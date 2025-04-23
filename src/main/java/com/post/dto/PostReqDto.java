package com.post.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostReqDto {
    String title;
    String content;
    String category;
    String postType;
    String address;
    String language;
    String emotion;
    List<String> tags;
}
