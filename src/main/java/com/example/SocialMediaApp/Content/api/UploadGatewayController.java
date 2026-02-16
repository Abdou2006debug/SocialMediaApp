package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Content.application.UploadGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/media/upload")
public class UploadGatewayController {

    private final UploadGatewayService uploadGatewayService;

    @GetMapping("/request")
    public String requestUpload(@AuthenticationPrincipal String currentUserId,uploadRequest request){
        return uploadGatewayService.requestUpload(currentUserId,request);
    }

    @DeleteMapping("/discard")
    public void discardUpload(@AuthenticationPrincipal String currentUserId,@RequestParam String filepath){
        uploadGatewayService.deleteUpload(currentUserId,filepath);
    }

    @PutMapping("/confirm")
    public void confirmUpload(@AuthenticationPrincipal String currentUserId,@RequestParam String filepath){
        uploadGatewayService.confirmUpload(currentUserId,filepath);
    }

}
