package com.example.SocialMediaApp.Content.api;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Content.application.PostManagementService;
import com.example.SocialMediaApp.Content.application.UploadGatewayService;
import com.example.SocialMediaApp.Storage.signResponse;
import com.example.SocialMediaApp.Storage.uploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/content/post")
public class PostManagementController {

    private final PostManagementService postManagementService;
    private final UploadGatewayService uploadGatewayService;

    @GetMapping("/upload")
    public String requestUploadPost(@RequestBody uploadRequest request){
        request.setUploadType(uploadType.POST);
        return uploadGatewayService.requestUpload(request);
    }

}
