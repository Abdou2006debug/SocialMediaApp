package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.application.StoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StoryQueryController {

    private final StoryQueryService storyQueryService;

}
