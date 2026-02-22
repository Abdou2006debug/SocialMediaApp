package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, String> {
}
