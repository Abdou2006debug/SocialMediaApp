package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.api.dto.StoryCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.StoryRepresentation;
import com.example.SocialMediaApp.Content.application.StoryLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/story")
public class StoryLifecycleController {

    private final StoryLifecycleService storyLifecycleService;


    @PostMapping
    public ResponseEntity<StoryRepresentation> createStory(@RequestBody @Valid StoryCreationRequest storyCreation){
        return ResponseEntity.ok(storyLifecycleService.createStory(storyCreation));
    }

    @PatchMapping("/{storyId}/publish")
    public ResponseEntity<Void> publishStory(@PathVariable String storyId){
        storyLifecycleService.publishStory(storyId);
        return ResponseEntity.noContent().build();
    }


}
