package com.example.post.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "translated_comment")
@NoArgsConstructor
public class TranslatedComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long translationCommentId;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(columnDefinition = "TEXT")
    private String content;
    private String language;
}
