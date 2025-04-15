package com.example.post.entity;
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
}
