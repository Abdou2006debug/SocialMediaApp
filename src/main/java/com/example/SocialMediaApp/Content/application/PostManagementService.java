package com.example.SocialMediaApp.Content.application;
import com.example.SocialMediaApp.Content.api.dto.postCreation;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class PostManagementService {

    private final UploadGatewayService uploadGateway;
    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;


    public void createPost(postCreation postCreation){

    }



}
