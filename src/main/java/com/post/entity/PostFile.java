package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "post_file")
@NoArgsConstructor
public class PostFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postFileId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String url;

    @Builder
    public PostFile(Long postFileId, Post post, String url) {
        this.postFileId = postFileId;
        this.post = post;
        this.url = url;
    }
}
