package com.post.repository;

import com.post.entity.ReplyReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyReactionRepository extends JpaRepository<ReplyReaction, Long> {
    long countByReply_ReplyIdAndOption(Long replyId, String option);

    ReplyReaction findByReply_ReplyIdAndUser_UserId(long replyId, long userId);
}
