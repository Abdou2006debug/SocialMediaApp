package com.example.SocialMediaApp.SocialGraph.application;

import com.example.SocialMediaApp.SocialGraph.application.cache.FollowCacheUpdater;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.Notification.domain.events.FollowNotification;
import com.example.SocialMediaApp.Profile.api.dto.profileDetails;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.Shared.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.Shared.Exceptions.NoRelationShipException;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.RelationshipStatus;
import com.example.SocialMediaApp.SocialGraph.domain.events.followAdded;
import com.example.SocialMediaApp.SocialGraph.domain.events.followRemoved;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FollowService {

    private final FollowRepo followRepo;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final FollowCacheUpdater followCacheUpdater;

    @CheckUserExistence
    public profileDetails Follow(String targetUserId) {
        String currentUserId = authenticatedUserService.getcurrentuser();
        if (currentUserId.equals(targetUserId)) {
            throw new BadFollowRequestException("you cant follow yourself");
        }
        if(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId, targetUserId, Follow.Status.ACCEPTED)){
            throw new BadFollowRequestException("Already followed");
        }
        if(blocksRepo.existsByBlockerIdAndBlockedId(targetUserId, currentUserId)||blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,targetUserId)){
            throw new BadFollowRequestException("You cant follow this User");
        }
       if(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId,targetUserId, Follow.Status.PENDING)){
           throw new BadFollowRequestException("request already sent");
       }
        Follow follow = new Follow(currentUserId,targetUserId);

        FollowNotification notification= new FollowNotification(currentUserId,targetUserId,
                FollowNotification.notificationType.FOLLOW);
        RelationshipStatus status;
        if (profileRepo.existsByUserIdAndIsprivateFalse(targetUserId)) {
            follow.setStatus(Follow.Status.ACCEPTED);
            follow.setFollowDate(Instant.now());
            log.info("publishing follow event for "+targetUserId);
            eventPublisher.publishEvent(notification);
            eventPublisher.publishEvent(new followAdded(follow));
            status=RelationshipStatus.FOLLOWING;
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS, currentUserId, FollowCacheUpdater.UpdateType.INCREMENT);
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS, targetUserId, FollowCacheUpdater.UpdateType.INCREMENT);
        } else {
            follow.setStatus(Follow.Status.PENDING);
            log.info("publishing follow request event for "+targetUserId);
            notification.setType(FollowNotification.notificationType.FOLLOW_REQUESTED);
            eventPublisher.publishEvent(notification);
            status=RelationshipStatus.FOLLOW_REQUESTED;
        }

        followRepo.save(follow);
        return new profileDetails(targetUserId,status);
    }

    // this method works for both pending and accepted followings
    @CheckUserExistence
    public void UnFollow(String targetUserId) {
        String currentUserId = authenticatedUserService.getcurrentuser();
        Follow follow = followRepo.findByFollowerIdAndFollowingId( currentUserId,targetUserId).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));
        followRepo.delete(follow);
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            eventPublisher.publishEvent(new followRemoved(follow));
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS,currentUserId, FollowCacheUpdater.UpdateType.DECREMENT);
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS, targetUserId, FollowCacheUpdater.UpdateType.DECREMENT);
        }
    }

    // this method works for both pending and accepted followers
    @CheckUserExistence
    public void removefollower(String targetUserId) {
        String  currentUserId= authenticatedUserService.getcurrentuser();
        Follow follow = followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId).
                orElseThrow(()->new NoRelationShipException("No relationship with user found"));
        followRepo.delete(follow);
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            eventPublisher.publishEvent(new followRemoved(follow));
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS,currentUserId, FollowCacheUpdater.UpdateType.DECREMENT);
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS, targetUserId, FollowCacheUpdater.UpdateType.DECREMENT);
        }else{
            FollowNotification notification=new FollowNotification(currentUserId,targetUserId,
                    FollowNotification.notificationType.FOLLOWING_REJECTED);
            eventPublisher.publishEvent(notification);
        }
    }
}


