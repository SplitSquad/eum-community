package com.example.post.repository;

import com.example.post.entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    long countByPost_postIdAndOption(long postId, String option);

    PostReaction findByUser_UserIdAndPost_PostId(long userId, Long postId);
}
