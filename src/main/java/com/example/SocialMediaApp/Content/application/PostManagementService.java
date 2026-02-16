package com.example.SocialMediaApp.Content.application;
import com.example.SocialMediaApp.Content.api.dto.postCreation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Storage.uploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostManagementService {

    private final UploadGatewayService uploadGateway;
    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final Contentmapper contentmapper;


    public void createPost(postCreation postCreation){
        String currentUserId=authenticatedUserService.getcurrentuser();
        List<String> filespaths=postCreation.getFilepaths();
        uploadGateway.finalizeUploads(currentUserId,filespaths, uploadType.POST);
        List<Media> mediaList=convertToMedia(filespaths);
        PostSettings postSettings=contentmapper.toPostSettings(postCreation);
        Post post= Post.builder().
                caption(postCreation.getCaption()).
                mediaList(mediaList).postSettings(postSettings).build();

        post.setUser(currentUserId);
        postRepo.save(post);
    }

    public List<Media> convertToMedia(List<String> filepaths){
       return filepaths.stream().map(filepath->{
            int last=filepath.lastIndexOf("/");
            String type=filepath.substring(last);
            Media.MediaType mediaType= type.equalsIgnoreCase("video")?
                    Media.MediaType.VIDEO: Media.MediaType.IMAGE;
            return new Media(filepath,mediaType);
        }).collect(Collectors.toList());
    }



}
