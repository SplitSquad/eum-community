package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
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

    @Builder
    public TranslatedComment(Long translationCommentId, Comment comment, String content, String language) {
        this.translationCommentId = translationCommentId;
        this.comment = comment;
        this.content = content;
        this.language = language;
    }
}
