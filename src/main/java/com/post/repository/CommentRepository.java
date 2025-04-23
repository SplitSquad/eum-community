package com.post.repository;

import com.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPost_PostId(long postId, Pageable pageable);

    List<Comment> findByPost_PostId(long postId);

    Page<Comment> findByUser_UserId(long userId, Pageable pageable);
}
