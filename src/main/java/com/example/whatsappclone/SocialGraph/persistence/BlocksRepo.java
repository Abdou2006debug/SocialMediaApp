package com.example.whatsappclone.SocialGraph.persistence;

import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.SocialGraph.domain.Blocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlocksRepo extends JpaRepository<Blocks,String> {
    Optional<Blocks> findByBlockedAndBlocker(User Blocked, User Blocker);
    List<Blocks> findByBlocker(User Blocker);
    boolean existsByBlockerAndBlocked(User Blocker, User Blocked);
}
