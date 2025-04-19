package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Builder
    public TranslatedReply(Long translationReplyId, Reply reply, String content, String language) {
        this.translationReplyId = translationReplyId;
        this.reply = reply;
        this.content = content;
        this.language = language;
    }
}
