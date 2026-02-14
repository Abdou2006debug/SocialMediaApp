package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Content.application.StoryManagementService;
import com.example.SocialMediaApp.Content.application.UploadGatewayService;
import com.example.SocialMediaApp.Storage.signResponse;
import com.example.SocialMediaApp.Storage.uploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/content/story")
public class StoryManagementController {

    private final StoryManagementService storyManagementService;
    private final UploadGatewayService uploadGatewayService;

    @GetMapping("/upload")
    public String requestUploadStory(uploadRequest request){
        request.setUploadType(uploadType.STORY);
        return uploadGatewayService.requestUpload(request);
    }
}
