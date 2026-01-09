package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.DTO.serverToclient.RelationshipStatus;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Profile.application.cache.ProfileCacheManager;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Services.CacheServices.CacheQueryService;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Shared.Exceptions.FollowListNotVisibleException;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFollowViewHelper {

    private final ProfileCacheManager profileCacheManager;
    private final CacheQueryService cacheQueryService;
    private final FollowCacheReader followCacheReader;
    private final FollowCacheWriter followCacheWriter;
    private final FollowRepo followRepo;
    private final BlocksRepo blocksRepo;
    private final ProfileRepo profileRepo;
    public enum Position {FOLLOWERS, FOLLOWINGS}


    public List<profileSummary> listCurrentUserPendingFollows(String userId, Position position, int page){
        Pageable pageable= PageRequest.of(page,10);
        Page<Follow> pendingFollowsPage=position==Position.FOLLOWERS?
                followRepo.findByFollowingAndStatus(new User(userId), Follow.Status.PENDING,pageable):
                followRepo.findByFollowerAndStatus(new User(userId), Follow.Status.PENDING,pageable);
        List<Follow> pendingFollows=pendingFollowsPage.getContent();
        List<String> followsIds=pendingFollows.stream().
                map(follow -> position==Position.FOLLOWERS?follow.getFollower_id():follow.getFollowing_id()).toList();
       List<profileSummary> profileSummaries= buildProfileSummaries(followsIds);
        resolveCurrentUserFollowRelationShip(profileSummaries,userId,position, Follow.Status.PENDING);
        return profileSummaries;
    }

    public List<profileSummary> listCurrentUserFollows(String userId, Position position, int page){
     List<String> followsIds= getFollowsIdsfetch(userId,position,page);
     List<profileSummary> profileSummaries= buildProfileSummaries(followsIds);
     resolveCurrentUserFollowRelationShip(profileSummaries,userId,position, Follow.Status.ACCEPTED);
     return profileSummaries;
       }

       public List<profileSummary> listUserFollows(String viewerId,String targetedId, Position position, int page){
           List<String> followsIds= getFollowsIdsfetch(targetedId,position,page);
          List<profileSummary> profileSummaries=  buildProfileSummaries(followsIds);
          resolveViewerFollowRelationShip(profileSummaries,viewerId);
            return profileSummaries;
       }


       private void resolveCurrentUserFollowRelationShip(List<profileSummary> profileSummaries, String viewerId, Position position,Follow.Status followStatus){
           Map<String, profileSummary> summaryMap = profileSummaries.stream()
                   .collect(Collectors.toMap(profileSummary::getUserId, Function.identity()));

           List<String> targetUserIds = profileSummaries.stream()
                   .map(profileSummary::getUserId)
                   .toList();
           if(position==Position.FOLLOWERS){
               if(followStatus== Follow.Status.ACCEPTED){
                   profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOWED));
               }else{
                   profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUEST_RECEIVED));
               }
               List<Follow> followers =followRepo.findByFollower_IdAndFollowing_IdIn(viewerId,targetUserIds);
               for(Follow follow: followers){
                  profileSummary profileSummary= summaryMap.get(follow.getFollowing_id());
                  if(profileSummary!=null){
                      if(follow.getStatus()== Follow.Status.ACCEPTED){
                          profileSummary.setStatus(RelationshipStatus.FOLLOWING);
                      }else{
                          profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUESTED);
                      }
                  }
               }
               return;
           }
           if(followStatus== Follow.Status.ACCEPTED){
               profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOWING));
           }else{
               profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUESTED));
           }
           List<Follow> followings=followRepo.findByFollowing_IdAndFollower_IdIn(viewerId,targetUserIds);
           for(Follow follow: followings){
               profileSummary profileSummary= summaryMap.get(follow.getFollower_id());
               if(profileSummary!=null){
                   if(follow.getStatus()== Follow.Status.ACCEPTED){
                       profileSummary.setStatus(RelationshipStatus.FOLLOWED);
                   }else{
                       profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUEST_RECEIVED);
                   }
               }
           }
       }

    private void resolveViewerFollowRelationShip(List<profileSummary> profileSummaries, String viewerId) {
        Map<String, profileSummary> summaryMap = profileSummaries.stream()
                .collect(Collectors.toMap(profileSummary::getUserId, Function.identity()));

        List<String> targetUserIds = profileSummaries.stream()
                .map(profileSummary::getUserId)
                .toList();
           profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.NOT_FOLLOWING));
        List<Follow> outgoing = followRepo.findByFollower_IdAndFollowing_IdIn(viewerId, targetUserIds);
        for (Follow follow : outgoing) {
            profileSummary summary = summaryMap.get(follow.getFollowing_id());
            if (summary != null) {
                RelationshipStatus status = follow.getStatus() == Follow.Status.PENDING
                        ? RelationshipStatus.FOLLOW_REQUESTED
                        : RelationshipStatus.FOLLOWING;
                summary.setStatus(status);
            }
        }


        List<Follow> incoming = followRepo.findByFollowing_IdAndFollower_IdIn(viewerId,targetUserIds);
        for (Follow follow : incoming) {
            profileSummary profileSummary = summaryMap.get(follow.getFollower_id());
            if (profileSummary != null && profileSummary.getStatus() == null) {
                RelationshipStatus status = follow.getStatus() == Follow.Status.PENDING
                        ? RelationshipStatus.FOLLOW_REQUEST_RECEIVED
                        : RelationshipStatus.FOLLOWED;
                profileSummary.setStatus(status);
            }
        }

    }

    private List<profileSummary> buildProfileSummaries(List<String> usersIds){
        List<profileSummary> profileSummaries=usersIds.stream().map(profileSummary::new).toList();
        Map<String, profileSummary> summaryMap = profileSummaries.stream()
                .collect(Collectors.toMap(profileSummary::getUserId, Function.identity()));
        // fetching from cache first
        usersIds.forEach(userId->{
            Optional<ProfileInfo> profileInfo= cacheQueryService.getProfileInfo(userId);
            if(profileInfo.isPresent()){
               profileSummary profileSummary=summaryMap.get(userId);
               ProfileInfo profileInfo1 = profileInfo.get();
               profileSummary.setAvatarurl(profileInfo1.getAvatarurl());
               profileSummary.setUsername(profileInfo1.getUsername());
            }
        });
        // filtering the non fetched profile summaries to fetch them from db and cache them also
        usersIds=summaryMap.entrySet().stream().
                filter(e->e.getValue().getUsername()==null).map(Map.Entry::getKey).toList();
        List<Profile> profiles= profileRepo.findByUserIdIn(usersIds);
        for(Profile profile:profiles){
            profileSummary profileSummary=summaryMap.get(profile.getUserId());
            profileSummary.setUsername(profile.getUsername());
            profileSummary.setAvatarurl(profile.getPublicavatarurl());
            profileCacheManager.cacheProfileInfo(profile);
        }

           return profileSummaries;
        }


        private List<String> getFollowsIdsfetch(String userId, Position position, int page){
           if(position==Position.FOLLOWERS){
               return followCacheReader.getuserCachedFollowers(userId,page).orElseGet(()->followCacheWriter.cacheUserFollowers(userId,page));
           }
            return followCacheReader.getusercachedfollowings(userId,page).orElseGet(()->followCacheWriter.cacheUserFollowings(userId,page));
}

        public void canViewUserFollows(User currentUser,User requestedUser,Position position){
            boolean isblocked=blocksRepo.existsByBlockerAndBlocked(requestedUser,currentUser);
            if(isblocked){
                throw  new FollowListNotVisibleException(String.format("%s list for user is not visible.",position.name()));
            }
            boolean hasblocked= blocksRepo.existsByBlockerAndBlocked(currentUser,requestedUser);
            if (hasblocked) {
                throw new FollowListNotVisibleException(String.format("%s list for user is not visible.",position.name()));
            }

            if(!profileRepo.existsByUserAndIsprivateFalse(requestedUser)){
                if(!followRepo.existsByFollowerAndFollowingAndStatus(currentUser,requestedUser, Follow.Status.ACCEPTED)){
                    throw new FollowListNotVisibleException(String.format("%s list for user is not visible: the profile is private.",position.name()));
                };
            }

        }
}

