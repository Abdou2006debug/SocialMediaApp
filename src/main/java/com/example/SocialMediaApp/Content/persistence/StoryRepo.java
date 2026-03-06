package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoryRepo extends JpaRepository<Story,String> {
    Optional<Story> findByUserIdAndStoryIdAndStoryStatus(String userId,String storyId,Story.StoryStatus storyStatus);
    @Query("SELECT s FROM Story s WHERE s.user.id=:userId AND s.storyStatus=:storyStatus AND s.expiresAt > CURRENT_TIMESTAMP")
    List<Story> getUserActiveStories(@Param("userId") String userId, @Param("storyStatus") Story.StoryStatus storyStatus);
}
