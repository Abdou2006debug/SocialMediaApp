package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.StoryCreation;
import com.example.SocialMediaApp.Content.api.dto.StoryCreationRequest;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.Story;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.UploadFinilazing;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryLifecycleService {

    private final UploadGatewayService uploadGateway;
    private final AuthenticatedUserService authenticatedUserService;
    private final StoryRepo storyRepo;

    public void createStory(StoryCreationRequest storyCreationRequest){
        String currentUserId=authenticatedUserService.getcurrentuser();
        List<String> uploadRequestsIds=storyCreationRequest.getStoryCreations().stream().map(StoryCreation::getUploadRequestId).toList();
       // UploadFinilazing uploadFinilazing =uploadGateway.finalizeUploads(currentUserId,uploadRequestsIds, UploadType.STORY);



    }

    public void publishStory(String storyId){
        String currentUserId=authenticatedUserService.getcurrentuser();
        Story draftStory=storyRepo.findByUserIdAndStoryIdAndStoryStatus(currentUserId,storyId, Story.StoryStatus.DRAFT).
                orElseThrow(()-> new ActionNotAllowedException("Action could not be completed"));
        draftStory.setPublishedAt(Instant.now());
        draftStory.setExpiresAt(Instant.now().plus(24,ChronoUnit.HOURS));
        draftStory.setStoryStatus(Story.StoryStatus.PUBLISHED);
        storyRepo.save(draftStory);
    }

    public void deleteStory(String storyId){
        String currentUserId=authenticatedUserService.getcurrentuser();
    }




}
