package com.example.post.repository;

import com.example.post.entity.TranslatedReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatedReplyRepository extends JpaRepository<TranslatedReply, Long> {
    TranslatedReply findByReply_ReplyIdAndLanguage(Long replyId, String language);
}
