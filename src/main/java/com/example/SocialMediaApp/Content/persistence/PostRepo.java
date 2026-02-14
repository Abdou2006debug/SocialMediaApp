package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepo extends JpaRepository<Post,String> {
}
