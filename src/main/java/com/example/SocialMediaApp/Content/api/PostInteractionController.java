package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.api.dto.CommentRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentResponse;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.application.PostInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/content/post")
public class PostInteractionController {

    private final PostInteractionService posttInteractionService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<LikeResponse> likePost(@PathVariable String postId){
        return ResponseEntity.ok(posttInteractionService.addPostLike(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> commentPost(@PathVariable String postId, @RequestBody @Valid CommentRequest commentRequest){
        return ResponseEntity.ok(posttInteractionService.addPostComment(postId,commentRequest));
    }

}
