package com.example.whatsappclone.Profile.persistence;

import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.Profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepo extends JpaRepository<Profile,String> {
    Optional<Profile> findByUser(User user);
    boolean existsByUserUsername(String username);
    boolean existsByUserAndIsprivateFalse(User user);
   List<Profile> findByUserIdIn(List<String> usersIds);
}
