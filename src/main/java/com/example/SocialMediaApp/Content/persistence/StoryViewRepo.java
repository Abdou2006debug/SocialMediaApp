package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryViewRepo extends JpaRepository<StoryView,String> {

}
