package com.post.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "translated_post")
public class TranslatedPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long translationPostId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String language;

    @Builder
    public TranslatedPost(Long translationPostId, String language, String content, String title, Post post) {
        this.translationPostId = translationPostId;
        this.language = language;
        this.content = content;
        this.title = title;
        this.post = post;
    }
}
