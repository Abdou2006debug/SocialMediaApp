package com.example.SocialMediaApp.Upload.api;

import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.UploadResponse;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.SupabaseWebhookPayload;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@Hidden
public class UploadGatewayController {

    private final UploadGatewayService uploadGatewayService;

    @GetMapping("/request")
    public ResponseEntity<UploadResponse>  requestUpload(@AuthenticationPrincipal String currentUserId, UploadRequest request){
        return ResponseEntity.ok(uploadGatewayService.requestUpload(currentUserId,request));
    }

    @DeleteMapping("/discard")
    public ResponseEntity<Void>  discardUpload(@AuthenticationPrincipal String currentUserId,@RequestParam String filepath){
        uploadGatewayService.deleteUpload(currentUserId,filepath);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmUpload(@RequestHeader("X-Webhook-Secret") String signature, @RequestBody SupabaseWebhookPayload webhookPayload){
        uploadGatewayService.confirmUpload(signature,webhookPayload);
        return ResponseEntity.noContent().build();
    }

}
