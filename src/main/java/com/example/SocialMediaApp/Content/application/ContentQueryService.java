package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentQueryService {

    private final PostRepo postRepo;
    private final StoryRepo storyRepo;

}
