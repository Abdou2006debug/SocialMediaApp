package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.PostLike;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostLikeRepo extends JpaRepository<PostLike, UUID> {
    boolean existsByPostIdAndUserId(String postId,String userId);
    void deleteByPostIdAndUserId(String postId,String userId);
}
