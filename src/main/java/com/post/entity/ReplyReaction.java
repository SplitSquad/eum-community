package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "reply_reaction")
@NoArgsConstructor
public class ReplyReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reply_id")
    private Reply reply;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "`option`")
    private String option;

    @Builder
    public ReplyReaction(Long id, Reply reply, User user, String option) {
        this.id = id;
        this.reply = reply;
        this.user = user;
        this.option = option;
    }
}
