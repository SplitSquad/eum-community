package com.example.post.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "post")
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String createdAt;
    private Long views;
    private String category;
    private Integer isFile;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().toString();
    }
}
