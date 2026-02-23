package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Like;
import com.example.SocialMediaApp.Content.domain.LikeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikeRepo extends JpaRepository<Like, UUID> {

    void deleteByUserIdAndTargetIdAndLikeType(String userId,String targetId,LikeType likeType);
    boolean existsByUserIdAndTargetIdAndLikeType(String userId, String targetId, LikeType likeType);
}
