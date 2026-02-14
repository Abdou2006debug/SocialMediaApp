package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepo extends JpaRepository<Story,String> {
}
