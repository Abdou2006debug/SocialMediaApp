package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepo extends JpaRepository<Profile,String> {
    Optional<Profile> findByUser(User user);
    boolean existsByUserUsername(String username);
    boolean existsByUserAndIsprivateFalse(User user);
}
