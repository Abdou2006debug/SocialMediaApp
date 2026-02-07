package com.example.SocialMediaApp.SocialGraph.application;

import com.example.SocialMediaApp.Notification.domain.events.FollowNotification;
import com.example.SocialMediaApp.SocialGraph.application.cache.FollowCacheUpdater;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.Shared.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.Shared.Exceptions.NoRelationShipException;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.events.followAdded;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FollowRequestService {

    private final FollowRepo followRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final Logger logger= LoggerFactory.getLogger(FollowRequestService.class);
    private final ApplicationEventPublisher eventPublisher;
    private final FollowCacheUpdater followCacheUpdater;

    @CheckUserExistence
    public void acceptFollow(String userId) {
        User currentuser =authenticatedUserService.getcurrentuser();
        User targetUser=new User(userId);
        Follow followRequest = followRepo.
                findByFollowerAndFollowing(targetUser,currentuser).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));
        if(followRequest.getStatus()== Follow.Status.ACCEPTED){
            throw new BadFollowRequestException("couldn't perform accept follow action on this user");
        }
        followRequest.setStatus(Follow.Status.ACCEPTED);
        followRequest.setFollowDate(Instant.now());
        followRepo.save(followRequest);
        logger.info("publishing following accepted event to "+targetUser.getUsername());
        eventPublisher.publishEvent(new FollowNotification(currentuser,targetUser,
                FollowNotification.notificationType.FOLLOWING_ACCEPTED));
        eventPublisher.publishEvent(new followAdded(followRequest));
        followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS,currentuser.getId(), FollowCacheUpdater.UpdateType.INCREMENT);
        followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS,targetUser.getId(), FollowCacheUpdater.UpdateType.INCREMENT);
    }

    @CheckUserExistence
    public void rejectFollow(String userId) {

        User currentUser = authenticatedUserService.getcurrentuser();
        User targetUser=new User(userId);
        Follow follow = followRepo.findByFollowerAndFollowing(targetUser,currentUser).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            throw new BadFollowRequestException("couldn't perform reject follow action on this user");
        }
        followRepo.delete(follow);
        logger.info("publishing following rejected event to "+targetUser.getUsername());
        eventPublisher.publishEvent(new FollowNotification(currentUser,targetUser,
                FollowNotification.notificationType.FOLLOWING_REJECTED));
    }

    @CheckUserExistence
    public void unsendFollowingRequest(String userId){
        User currentUser=authenticatedUserService.getcurrentuser();
        User targetUser=new User(userId);
    Follow followingRequest = followRepo.findByFollowerAndFollowing(currentUser,targetUser).
            orElseThrow(()->new NoRelationShipException("No relation with user found"));
        if (followingRequest.getStatus() == Follow.Status.ACCEPTED) {
        throw new BadFollowRequestException("couldn't perform unsend follow request on this user");
    }
        followRepo.delete(followingRequest);
}


}
