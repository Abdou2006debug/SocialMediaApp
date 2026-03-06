package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.StoryCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.StoryRepresentation;
import com.example.SocialMediaApp.Content.application.StoryLifecycleService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/story")
public class StoryLifecycleController {

    private final StoryLifecycleService storyLifecycleService;


    @GetMapping("/new")
    public void redirectPost(@AuthenticationPrincipal Jwt jwt, HttpServletResponse response){
        // this will redirect user to the page where he can upload files and create post
    }

    @PostMapping
    @Hidden
    public ResponseEntity<StoryRepresentation> createStory(@RequestBody @Valid StoryCreationRequest storyCreation){
        return ResponseEntity.ok(storyLifecycleService.createStory(storyCreation));
    }

    @PatchMapping("/{storyId}/publish")
    public ResponseEntity<Void> publishStory(@PathVariable String storyId){
        storyLifecycleService.publishStory(storyId);
        return ResponseEntity.noContent().build();
    }


}
