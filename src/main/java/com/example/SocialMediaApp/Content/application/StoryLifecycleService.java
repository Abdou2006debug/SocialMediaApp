package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.StoryCreationRequest;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Story;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Upload.application.StoryUploadService;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryLifecycleService {

    private final StoryUploadService storyUploadService;
    private final AuthenticatedUserService authenticatedUserService;
    private final StoryRepo storyRepo;


    public void createStory(StoryCreationRequest storyCreationRequest){
        String currentUserId=authenticatedUserService.getcurrentuser();
        String batchId=storyCreationRequest.getBatchId();
        List<Media> mediaList= storyUploadService.finalizeStoryUploads(currentUserId,batchId);



    }

    public void publishStory(String batchId){
        String currentUserId=authenticatedUserService.getcurrentuser();
        Story draftStory=storyRepo.findByUserIdAndStoryIdAndStoryStatus(currentUserId,batchId, Story.StoryStatus.DRAFT).
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
