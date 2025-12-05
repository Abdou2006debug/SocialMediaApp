package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User,String> {
    Optional<User> findByKeycloakId(String keycloakId);

    void deleteByUsername(String s);
}
