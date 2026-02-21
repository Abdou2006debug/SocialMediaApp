package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.api.dto.postCreation;
import com.example.SocialMediaApp.Content.application.PostLifecycleService;
import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/content/post")
@Validated
public class PostManagementController {

    private final PostLifecycleService postLifecycleService;

    @PostMapping
    public void createPost(@RequestBody @Valid postCreation postCreation){
        postLifecycleService.createPost(postCreation);
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable  String postId){
        postLifecycleService.deletePost(postId);
    }

    @PatchMapping("/{postId}/publish")
    public ResponseEntity<Void> publishPost(@PathVariable String postId) {
        postLifecycleService.publishPost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/visibility")
    public ResponseEntity<Void> toggleVisibility(@PathVariable String postId,
                                                 @RequestParam Post.PostStatus status) {
        postLifecycleService.togglePostVisibility(postId, status);
        return ResponseEntity.noContent().build();
    }

}
