package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.application.StoryLifecycleService;
import com.example.SocialMediaApp.Upload.application.StoryUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/story")
public class StoryLifecycleController {

    private final StoryLifecycleService storyLifecycleService;
    private final StoryUploadService storyUploadService;

    @GetMapping("/batch")
    public ResponseEntity<String> requestStoryBatch(@AuthenticationPrincipal String currentUserId){
        return ResponseEntity.ok(storyUploadService.requestStoryBatch(currentUserId));
    }


}
