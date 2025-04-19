package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "comment")
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String createdAt;
    private Long replyCnt;
    private Long heart;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().toString();
    }

    @Builder
    public Comment(Long commentId, Post post, User user, String createdAt, Long heart, Long replyCnt) {
        this.commentId = commentId;
        this.post = post;
        this.user = user;
        this.createdAt = createdAt;
        this.heart = heart;
        this.replyCnt = replyCnt;
    }
}
