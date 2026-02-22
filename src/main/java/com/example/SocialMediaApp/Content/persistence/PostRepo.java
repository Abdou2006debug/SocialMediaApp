package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepo extends JpaRepository<Post,String> {
    boolean existsByUserIdAndPostIdAndPostStatus(String userId, String postId, Post.PostStatus postStatus);

    @Modifying
    @Transactional
    @Query("update Post p set p.postStatus = :status where " +
            "p.id = :postId and p.postStatus in :allowedStatuses and p.user.id = :userId")
    int updatePostStatus(@Param("postId") String postId, @Param("status") Post.PostStatus status,
                         @Param("userId") String userId, @Param("allowedStatuses") List<Post.PostStatus> allowedStatuses);

    Optional<Post> findByUserIdAndPostIdAndPostStatus(String userId,String postId,Post.PostStatus postStatus);
    @Modifying
    @Transactional
    @Query(value = "UPDATE posts SET likes = likes + :delta WHERE id = :postId RETURNING likes",nativeQuery = true)
    long updatePostLikes(@Param("postId") String postId,@Param("delta") long delta);

}
