package com.example.SocialMediaApp.Content.application;
import com.example.SocialMediaApp.Content.api.dto.PostCreation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.uploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PostLifecycleService {

    private final UploadGatewayService uploadGateway;
    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final Contentmapper contentmapper;


    public void createPost(PostCreation postCreation){
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

    // publishing post for first time draft -> published
    public void publishPost(String postId){
        String currentUserId=authenticatedUserService.getcurrentuser();
        Post draftPost=postRepo.findByUserIdAndPostIdAndPostStatus(currentUserId,postId, Post.PostStatus.DRAFT).
                orElseThrow(()-> new ActionNotAllowedException("Action could not be completed"));
        draftPost.setPublishedAt(Instant.now());
        draftPost.setPostStatus(Post.PostStatus.PUBLISHED);
        postRepo.save(draftPost);
    }

    // switching between published <-> unpublished
    public void togglePostVisibility(String postId,Post.PostStatus status){
       List<Post.PostStatus> allowedStatus=List.of(Post.PostStatus.PUBLISHED, Post.PostStatus.UNPUBLISHED);
        if(!allowedStatus.contains(status)){
            throw new ActionNotAllowedException("Action could not be completed");
        }
        String currentUserId=authenticatedUserService.getcurrentuser();
        int updated= postRepo.updatePostStatus(postId,status,currentUserId,allowedStatus);
        if(updated==0){
            // can be thrown if post not found or user don't have access or post status is originally in draft or deleted
            throw new ActionNotAllowedException("Action could not be completed");
        }
    }


    public void deletePost(String postId){
        String currentUserId=authenticatedUserService.getcurrentuser();
        int updated=postRepo.updatePostStatus(postId, Post.PostStatus.DELETED,currentUserId,List.of(Post.PostStatus.values()));
        if(updated==0){
            throw new ActionNotAllowedException("Action could not be completed");
        }
    }




}
