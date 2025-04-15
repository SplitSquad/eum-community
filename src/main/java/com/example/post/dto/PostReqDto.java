package com.example.post.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostReqDto {
    String title;
    String content;
    String category;
    String language;
    String emotion;
    List<String> tags;
}
