package com.example.post.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "translated_reply")
@NoArgsConstructor
public class TranslatedReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long translationReplyId;

    @ManyToOne
    @JoinColumn(name = "reply_id")
    private Reply reply;

    @Column(columnDefinition = "TEXT")
    private String content;
    private String language;
}
