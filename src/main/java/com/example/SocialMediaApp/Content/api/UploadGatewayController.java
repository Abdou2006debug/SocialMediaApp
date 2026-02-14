package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.api.dto.uploadAction;
import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Content.application.UploadGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/media/upload")
public class UploadGatewayController {

    private final UploadGatewayService uploadGatewayService;

    @GetMapping
    public String requestUpload(uploadRequest request){
        return uploadGatewayService.requestUpload(request);
    }

    @DeleteMapping
    public void discardUpload(@RequestParam String filepath){
        uploadGatewayService.handleUpload(filepath, uploadAction.DELETE);
    }

    @PutMapping
    public void confirmUpload(@RequestParam String filepath){
        uploadGatewayService.confirmUpload(filepath);
    }


}
