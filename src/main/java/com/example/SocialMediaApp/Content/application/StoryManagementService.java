package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoryManagementService {

    private final UploadGatewayService uploadGateway;
    private final AuthenticatedUserService authenticatedUserService;
    private final StoryRepo storyRepo;

    public void createStory(uploadRequest uploadRequest){

    }



}
