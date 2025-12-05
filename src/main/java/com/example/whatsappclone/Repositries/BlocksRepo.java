package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlocksRepo extends JpaRepository<Blocks,String> {
    Optional<Blocks> findByBlockedAndBlocker(User Blocked, User Blocker);
    List<Blocks> findByBlocker(User Blocker);

    boolean existsByBlockedAndBlocker(User usertoblock, User currentuser);
}
