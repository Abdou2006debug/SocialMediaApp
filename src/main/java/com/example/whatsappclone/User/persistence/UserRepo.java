package com.example.whatsappclone.User.persistence;

import com.example.whatsappclone.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepo extends JpaRepository<User,String> {
}
