package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.Profile.application.ProfileSummaryBuilder;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.SocialGraph.application.cache.FollowCacheWriter;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowQueryHelper {

    private final FollowCacheWriter followCacheWriter;
    private final FollowRepo followRepo;
    private final FollowRelationShipResolver followRelationShipResolver;
    private final ProfileSummaryBuilder profileSummaryService;
    public enum Position {FOLLOWERS, FOLLOWINGS}

    // no cache is supposed to be here since its  viewed  by the owing user only so fetching directly from db
    public List<profileSummary> listCurrentUserPendingFollows(String userId, Position position, int page){
        Pageable pageable= PageRequest.of(page,10);
        Page<Follow> pendingFollowsPage=position==Position.FOLLOWERS?
                followRepo.findByFollowingAndStatus(new User(userId), Follow.Status.PENDING,pageable):
                followRepo.findByFollowerAndStatus(new User(userId), Follow.Status.PENDING,pageable);
        List<Follow> pendingFollows=pendingFollowsPage.getContent();
        List<String> followsIds=pendingFollows.stream().
                map(follow -> position==Position.FOLLOWERS?follow.getFollower_id():follow.getFollowing_id()).toList();
       List<profileSummary> profileSummaries= profileSummaryService.buildProfileSummaries(followsIds);
        followRelationShipResolver.
                resolveCurrentUserFollowRelationShip(profileSummaries,userId,position, Follow.Status.PENDING);
        return profileSummaries;
    }

    public List<profileSummary> listCurrentUserFollows(String userId, Position position, int page){
     Set<String> followsIds= followCacheWriter.cacheWindow(userId,page,position);
     List<profileSummary> profileSummaries= profileSummaryService.buildProfileSummaries(followsIds.stream().toList());
     followRelationShipResolver.
             resolveCurrentUserFollowRelationShip(profileSummaries,userId,position, Follow.Status.ACCEPTED);
     return profileSummaries;
       }

       public List<profileSummary> listUserFollows(String viewerId,String targetedId, Position position, int page){
           Set<String> followsIds= followCacheWriter.cacheWindow(targetedId,page,position);
          List<profileSummary> profileSummaries=  profileSummaryService.buildProfileSummaries(followsIds.stream().toList());
          followRelationShipResolver.
                  resolveViewerFollowRelationShip(profileSummaries,viewerId);
            return profileSummaries;
       }

}


