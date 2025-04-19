package com.post.repository;

import com.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE " +
            "(:category = '전체' OR p.category = :category) AND " +
            "(:region = '전체' OR p.user.address LIKE CONCAT('%', :region, '%'))")
    Page<Post> findByCategoryAndRegion(
            @Param("category") String category,
            @Param("region") String region,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Post p WHERE " +
            "(:category = '전체' OR p.category = :category) AND " +
            "(:region = '전체' OR p.user.address LIKE CONCAT('%', :region, '%'))")
    long countByCategoryAndRegion(
            @Param("category") String category,
            @Param("region") String region
    );

    Page<Post> findByUser_UserId(long userId, Pageable pageable);
}
