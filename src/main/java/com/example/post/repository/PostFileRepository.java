package com.example.post.repository;

import com.example.post.entity.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostFileRepository extends JpaRepository<PostFile, Long> {
    List<PostFile> findByPost_postId(long postId);

    void deleteByPost_PostId(Long postId);
}
