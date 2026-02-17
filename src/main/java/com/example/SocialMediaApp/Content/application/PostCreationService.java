package com.example.SocialMediaApp.Content.application;
import com.example.SocialMediaApp.Content.api.dto.postCreation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.uploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostCreationService {

    private final UploadGatewayService uploadGateway;
    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final Contentmapper contentmapper;


    public void createPost(postCreation postCreation){
        String currentUserId=authenticatedUserService.getcurrentuser();
        List<String> uploadRequestsIds=postCreation.getUploadRequestsIds();
        List<Media> mediaList=uploadGateway.finalizeUploads(currentUserId,uploadRequestsIds,uploadType.POST);
        PostSettings postSettings=contentmapper.toPostSettings(postCreation);
        Post post= Post.builder().
                caption(postCreation.getCaption()).
                mediaList(mediaList).postSettings(postSettings).build();

        post.setUser(currentUserId);
        postRepo.save(post);
    }

    // from draft to published
    public void confirmPost(String postId){
        String currentUserId=authenticatedUserService.getcurrentuser();
        boolean exists=postRepo.existsByUserIdAndPostIdAndPostStatus(currentUserId,postId, Post.PostStatus.DRAFT);
        if(!exists) throw new RuntimeException();

    }



}
