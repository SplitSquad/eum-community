package com.post.repository;

import com.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPost_PostId(long postId, Pageable pageable);

    Page<Comment> findByUser_UserId(long userId, Pageable pageable);
}
