package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.CommentRequest;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.application.CommentInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentInteractionController {

    private final CommentInteractionService commentInteractionService;

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<LikeResponse> likeComment(@PathVariable String commentId){
       return ResponseEntity.ok(commentInteractionService.addCommentLike(commentId));
    }


    @PostMapping("/{commentId}/replies")
    public ResponseEntity<Void>  replyComment(@PathVariable String commentId, @RequestBody @Valid CommentRequest commentRequest){
        commentInteractionService.addCommentReply(commentId,commentRequest);
        return ResponseEntity.noContent().build();
    }


}
