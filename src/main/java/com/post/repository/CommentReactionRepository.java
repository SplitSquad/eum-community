package com.post.repository;

import com.post.entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    long countByComment_CommentIdAndOption(Long commentId, String option);

    CommentReaction findByComment_CommentIdAndUser_UserId(long commentId, long userId);
}
