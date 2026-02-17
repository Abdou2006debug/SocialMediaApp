package com.example.SocialMediaApp.Upload.api;

import com.example.SocialMediaApp.Upload.api.dto.uploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.uploadResponse;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/media/upload")
@Hidden
public class UploadGatewayController {

    private final UploadGatewayService uploadGatewayService;

    @GetMapping("/request")
    public uploadResponse requestUpload(@AuthenticationPrincipal String currentUserId, uploadRequest request){
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
