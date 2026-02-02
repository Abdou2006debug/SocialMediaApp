package com.example.whatsappclone.SocialGraph.persistence;

import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.SocialGraph.domain.Blocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlocksRepo extends JpaRepository<Blocks, UUID> {
    Optional<Blocks> findByBlockedAndBlocker(User Blocked, User Blocker);
    boolean existsByBlockerAndBlocked(User Blocker, User Blocked);
}
