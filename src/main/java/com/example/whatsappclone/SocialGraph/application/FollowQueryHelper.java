package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.SocialGraph.application.cache.FollowCacheReader;
import com.example.whatsappclone.SocialGraph.application.cache.FollowCacheWriter;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowQueryHelper {

    private final FollowCacheReader followCacheReader;
    private final FollowCacheWriter followCacheWriter;
    private final FollowRepo followRepo;
    private final BlocksRepo blocksRepo;
    private final ProfileRepo profileRepo;
    private final FollowRelationShipResolver followRelationShipResolver;
    private final ProfileSummaryService profileSummaryService;
    public enum Position {FOLLOWERS, FOLLOWINGS}

    // no cache is supposed to be here since its not frequently viewed  so fetching directly from db
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
     List<String> followsIds= getFollowsIdsfetch(userId,position,page);
     List<profileSummary> profileSummaries= profileSummaryService.buildProfileSummaries(followsIds);
     followRelationShipResolver.
             resolveCurrentUserFollowRelationShip(profileSummaries,userId,position, Follow.Status.ACCEPTED);
     return profileSummaries;
       }

       public List<profileSummary> listUserFollows(String viewerId,String targetedId, Position position, int page){
           List<String> followsIds= getFollowsIdsfetch(targetedId,position,page);
          List<profileSummary> profileSummaries=  profileSummaryService.buildProfileSummaries(followsIds);
          followRelationShipResolver.
                  resolveViewerFollowRelationShip(profileSummaries,viewerId);
            return profileSummaries;
       }


        private List<String> getFollowsIdsfetch(String userId, Position position, int page){
           if(position==Position.FOLLOWERS){
               return followCacheReader.getuserCachedFollowers(userId,page).orElseGet(()->followCacheWriter.cacheUserFollowers(userId,page));
           }
            return followCacheReader.getusercachedfollowings(userId,page).orElseGet(()->followCacheWriter.cacheUserFollowings(userId,page));
}


}

