package com.example.whatsappclone.Identity.persistence;

import com.example.whatsappclone.Identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepo extends JpaRepository<User,String> {
}
