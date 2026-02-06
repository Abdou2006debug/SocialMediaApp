package com.example.SocialMediaApp.SocialGraph.persistence;

import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.SocialGraph.domain.Blocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BlocksRepo extends JpaRepository<Blocks, UUID> {
    Optional<Blocks> findByBlockedAndBlocker(User Blocked, User Blocker);
    boolean existsByBlockerAndBlocked(User Blocker, User Blocked);
}
