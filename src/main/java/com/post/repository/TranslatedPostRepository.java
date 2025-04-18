package com.post.repository;

import com.post.entity.TranslatedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatedPostRepository extends JpaRepository<TranslatedPost, Long> {
    TranslatedPost findByPost_PostIdAndLanguage(Long postId, String language);

    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.user.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND tp.title LIKE CONCAT('%', :keyword, '%')")
    Page<TranslatedPost> findByCategoryAndRegionAndTitle(
            @Param("category") String category,
            @Param("region")String region,
            @Param("keyword")String keyword,
            @Param("language")String language,
            Pageable pageable);

    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.user.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND tp.content LIKE CONCAT('%', :keyword," +
            " '%')")
    Page<TranslatedPost> findByCategoryAndRegionAndContent(
            @Param("category") String category,
            @Param("region") String region,
            @Param("keyword")String keyword,
            @Param("language") String language,
            Pageable pageable);

    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.user.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND (tp.title LIKE CONCAT('%', :keyword, '%') OR tp.content LIKE CONCAT('%', :keyword, '%'))")
    Page<TranslatedPost> findByCategoryAndRegionAndTitleOrContent(
            @Param("category") String category,
            @Param("region") String region,
            @Param("keyword") String keyword,
            @Param("language") String language,
            Pageable pageable);

    // 작성자 검색
    @Query("SELECT tp FROM TranslatedPost tp WHERE " +
            "(:category = '전체' OR tp.post.category = :category) AND " +
            "(:region = '전체' OR tp.post.user.address LIKE CONCAT('%', :region, '%')) AND " +
            "tp.language = :language AND tp.post.user.name LIKE CONCAT('%', :username, '%')")
    Page<TranslatedPost> findByCategoryAndRegionAndUsername(
            @Param("category") String category,
            @Param("region") String region,
            @Param("username") String username,
            @Param("language") String language,
            Pageable pageable);

    @Query("SELECT tp FROM TranslatedPost tp " +
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
            Pageable pageable);
}
