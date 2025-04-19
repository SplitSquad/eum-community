package com.post.repository;

import com.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByName(String tagName);

    @Query("SELECT pt.tag FROM PostTag pt WHERE pt.post.postId = :postId")
    List<Tag> findTagsByPostId(@Param("postId") Long postId);
}
