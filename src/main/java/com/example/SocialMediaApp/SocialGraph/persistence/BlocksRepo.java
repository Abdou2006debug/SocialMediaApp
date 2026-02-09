package com.example.SocialMediaApp.SocialGraph.persistence;

import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.SocialGraph.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlocksRepo extends JpaRepository<Block, UUID> {
    void deleteByBlockerIdAndBlockedId(String blockerId,String blockedId);
    boolean existsByBlockerIdAndBlockedId(String blockerId,String blockedId);
}
