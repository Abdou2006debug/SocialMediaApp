package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User,String> {
    Optional<User> findByKeycloakId(String keycloakId);
}
