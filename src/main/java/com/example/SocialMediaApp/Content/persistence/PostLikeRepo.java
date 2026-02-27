package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.PostLike;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PostLikeRepo extends JpaRepository<PostLike, UUID> {
    boolean existsByPostIdAndUserId(String postId,String userId);
    void deleteByPostIdAndUserId(String postId,String userId);
    List<PostLike> findByUserIdAndPostIdIn(String userId,List<String> postIds);
    @Query(value="SELECT post_id FROM PostLike WHERE user_id=:userId AND post_id IN :postIds",nativeQuery = true)
    Set<String> getLikesPostIds(@Param("userId") String userId,@Param("postIds") List<String> postIds);

}
