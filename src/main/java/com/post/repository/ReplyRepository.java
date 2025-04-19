package com.post.repository;

import com.post.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByComment_CommentId(long commentId);

    Reply findByReplyId(long replyId);
}
