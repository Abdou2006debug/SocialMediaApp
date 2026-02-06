package com.example.SocialMediaApp.User.persistence;

import com.example.SocialMediaApp.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepo extends JpaRepository<User,String> {
    boolean existsUserByUsername(String username);

    Optional<User> findByUsername(String username);
}
