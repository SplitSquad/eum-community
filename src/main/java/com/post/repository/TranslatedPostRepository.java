package com.post.repository;

import com.post.entity.TranslatedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslatedPostRepository extends JpaRepository<TranslatedPost, Long> {
    TranslatedPost findByPost_PostIdAndLanguage(Long postId, String language);

    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND tp.post.postType = :postType AND tp.title LIKE CONCAT('%', :keyword, '%')")
    Page<TranslatedPost> findByCategoryAndRegionAndPostTypeAndTitle(
            @Param("category") String category,
            @Param("region") String region,
            @Param("keyword") String keyword,
            @Param("language") String language,
            @Param("postType") String postType,
            Pageable pageable);

    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND tp.post.postType = :postType AND tp.content LIKE CONCAT('%', :keyword, '%')")
    Page<TranslatedPost> findByCategoryAndRegionAndPostTypeAndContent(
            @Param("category") String category,
            @Param("region") String region,
            @Param("keyword") String keyword,
            @Param("language") String language,
            @Param("postType") String postType,
            Pageable pageable);

    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND tp.post.postType = :postType AND " +
            "(tp.title LIKE CONCAT('%', :keyword, '%') OR tp.content LIKE CONCAT('%', :keyword, '%'))")
    Page<TranslatedPost> findByCategoryAndRegionAndPostTypeAndTitleOrContent(
            @Param("category") String category,
            @Param("region") String region,
            @Param("keyword") String keyword,
            @Param("language") String language,
            @Param("postType") String postType,
            Pageable pageable);

    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND tp.post.postType = :postType AND tp.post.user.name LIKE CONCAT('%', :username, '%')")
    Page<TranslatedPost> findByCategoryAndRegionAndPostTypeAndUsername(
            @Param("category") String category,
            @Param("region") String region,
            @Param("username") String username,
            @Param("language") String language,
            @Param("postType") String postType,
            Pageable pageable);

    /*@Query("SELECT tp FROM TranslatedPost tp " +
            "JOIN tp.post p " +
            "JOIN PostTag pt ON pt.post = p " +
            "JOIN Tag t ON pt.tag = t " +
            "WHERE t.name = :tag " +
            "AND tp.language = :language " +
            "AND p.createdAt >= :sevenDaysAgo " +
            "ORDER BY p.views DESC")
    Page<TranslatedPost> findTopByTagAndLanguageAndRecentDate(
            @Param("tag") String tag,
            @Param("language") String language,
            @Param("sevenDaysAgo") String sevenDaysAgo,
            Pageable pageable);*/

    @Query(value =
                    "SELECT tp.* FROM translated_post tp " +
                    "JOIN post p ON tp.post_id = p.post_id " +
                    "JOIN post_tag pt ON pt.post_id = p.post_id " +
                    "JOIN tag t ON pt.tag_id = t.tag_id " +
                    "WHERE t.name = :tag " +
                    "AND tp.language = :language " +
                    "AND p.created_at >= :sevenDaysAgo " +
                    "AND p.address = :address " +
                    "ORDER BY RAND() " +
                    "LIMIT 3",
            nativeQuery = true)
    List<TranslatedPost> findRandomTop3ByTagAndLanguageAndRecentDateAndAddress(
            @Param("tag") String tag,
            @Param("language") String language,
            @Param("sevenDaysAgo") String sevenDaysAgo,
            @Param("address") String address);
}
