package com.post.repository;

import com.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByUser_UserId(long userId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN PostTag pt ON p.postId = pt.post.postId " +
            "JOIN Tag t ON pt.tag.tagId = t.tagId " +
            "WHERE (:category = '전체' OR p.category = :category) " +
            "AND (:address = '전체' OR p.address LIKE CONCAT(:address, '%')) " +
            "AND p.postType = :postType " +
            "AND t.name IN :tagNames")
    Page<Post> findByCategoryAndAddressLikeAndPostTypeAndTagNamesIn(
            @Param("category") String category,
            @Param("address") String address,
            @Param("postType") String postType,
            @Param("tagNames") List<String> tagNames,
            Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE (:category = '전체' OR p.category = :category) " +
            "AND (:address = '전체' OR p.address LIKE CONCAT(:address, '%')) " +
            "AND p.postType = :postType")
    Page<Post> findByCategoryAndAddressLikeAndPostType(
            @Param("category") String category,
            @Param("address") String address,
            @Param("postType") String postType,
            Pageable pageable);

}
