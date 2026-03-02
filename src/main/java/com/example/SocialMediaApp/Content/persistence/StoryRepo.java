package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryRepo extends JpaRepository<Story,String> {
    Optional<Story> findByUserIdAndStoryIdAndStoryStatus(String userId,String storyId,Story.StoryStatus storyStatus);
}
