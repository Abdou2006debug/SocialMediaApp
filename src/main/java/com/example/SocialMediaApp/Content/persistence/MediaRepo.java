package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepo extends JpaRepository<Media,String>{
    List<Media>  findByPostIdIn(List<String> postIds);
    List<Media>  findByStoryIn(List<String> storyIds);
}
