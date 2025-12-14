package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User,String> {
    Optional<User> findByKeycloakId(String keycloakId);
Optional<User> findByUsername(String username);
    void deleteByUsername(String s);
}
