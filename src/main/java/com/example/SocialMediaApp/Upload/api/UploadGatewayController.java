package com.example.SocialMediaApp.Upload.api;

import com.example.SocialMediaApp.Upload.api.dto.PostUploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.PostUploadResponse;
import com.example.SocialMediaApp.Upload.api.dto.StoryUploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.StoryUploadResponse;
import com.example.SocialMediaApp.Upload.application.MediaUploadService;
import com.example.SocialMediaApp.Upload.application.PostUploadService;
import com.example.SocialMediaApp.Upload.application.StoryUploadService;
import com.example.SocialMediaApp.Upload.domain.SupabaseWebhookPayload;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@Hidden
public class UploadGatewayController {

    private final MediaUploadService mediaUploadService;
    private final PostUploadService postUploadService;
    private final StoryUploadService storyUploadService;

    @PostMapping("/post")
    public ResponseEntity<PostUploadResponse>  requestPostUpload(@AuthenticationPrincipal String currentUserId, PostUploadRequest request){
        return ResponseEntity.ok(postUploadService.requestPostUpload(currentUserId,request));
    }
    @PostMapping("/story")
    public ResponseEntity<StoryUploadResponse> requestStoryUpload(@AuthenticationPrincipal String currentUserId, StoryUploadRequest request){
        return ResponseEntity.ok(storyUploadService.requestStoryUpload(currentUserId,request));
    }

    @DeleteMapping("/discard")
    public ResponseEntity<Void>  discardUpload(@AuthenticationPrincipal String currentUserId,@RequestParam String filepath){
        mediaUploadService.deleteUpload(currentUserId,filepath);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmUpload(@RequestHeader("X-Webhook-Secret") String signature, @RequestBody SupabaseWebhookPayload webhookPayload){
        mediaUploadService.confirmUpload(signature,webhookPayload);
        return ResponseEntity.noContent().build();
    }

}
