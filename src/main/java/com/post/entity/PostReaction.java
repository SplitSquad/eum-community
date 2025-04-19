package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "post_reaction")
@NoArgsConstructor
public class PostReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "`option`")
    private String option;

    @Builder
    public PostReaction(Long id, Post post, User user, String option) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.option = option;
    }
}
