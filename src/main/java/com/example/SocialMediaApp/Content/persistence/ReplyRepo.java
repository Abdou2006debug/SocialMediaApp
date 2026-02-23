package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReplyRepo extends JpaRepository<Reply, UUID> {

}
