package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "reply")
@NoArgsConstructor
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long replyId;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().toString();
    }

    @Builder
    public Reply(Long replyId, Comment comment, User user, String createdAt) {
        this.replyId = replyId;
        this.comment = comment;
        this.user = user;
        this.createdAt = createdAt;
    }
}
