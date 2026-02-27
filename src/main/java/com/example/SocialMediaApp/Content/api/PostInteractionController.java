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
@RequestMapping("/api/v1/content/post")
public class PostInteractionController {

    private final PostInteractionService postInteractionService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<LikeResponse> likePost(@PathVariable String postId){
        return ResponseEntity.ok(postInteractionService.addPostLike(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> commentPost(@PathVariable String postId, @RequestBody @Valid CommentRequest commentRequest){
        return ResponseEntity.ok(postInteractionService.addPostComment(postId,commentRequest));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String postId,@PathVariable String commentId){
        postInteractionService.removePostComment(commentId);
        return ResponseEntity.noContent().build();
    }

}
