package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, String> {
    int deleteByIdAndUserId(String userId,String commentId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Comment SET likeCount = likeCount + :delta WHERE id = :commentId RETURNING likeCount",nativeQuery = true)
    long updateCommentLikes(@Param("commentId") String commentId, @Param("delta") long delta);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Comment SET replyCount = replyCount + :delta WHERE id = :commentId RETURNING replyCount",nativeQuery = true)
    long updateCommentReplies(@Param("commentId") String commentId, @Param("delta") long delta);
}
