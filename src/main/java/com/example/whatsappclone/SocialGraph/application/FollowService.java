package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Identity.persistence.UserRepo;
import com.example.whatsappclone.Notification.domain.events.notification;
import com.example.whatsappclone.Profile.api.dto.profileDetails;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Shared.CheckUserExistence;
import com.example.whatsappclone.Shared.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Shared.Exceptions.NoRelationShipException;
import com.example.whatsappclone.Shared.Exceptions.UserNotFoundException;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.domain.RelationshipStatus;
import com.example.whatsappclone.SocialGraph.domain.events.followAdded;
import com.example.whatsappclone.SocialGraph.domain.events.followRemoved;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
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

    @CheckUserExistence
    public profileDetails Follow(String userId) {
        User currentuser = authenticatedUserService.getcurrentuser(false);
        if (currentuser.getUuid().equals(userId)) {
            throw new BadFollowRequestException("you cant follow yourself");
        }
        User usertofollow=new User(userId);
        if(followRepo.existsByFollowerAndFollowingAndStatus(currentuser, usertofollow, Follow.Status.ACCEPTED)){
            throw new BadFollowRequestException("Already followed");
        }
        if(blocksRepo.existsByBlockerAndBlocked(usertofollow, currentuser)){
            throw new BadFollowRequestException("You cant follow this User");
        }
       if(blocksRepo.existsByBlockerAndBlocked(currentuser,usertofollow)){
           throw new BadFollowRequestException("You cant follow this User");
       }
       if(followRepo.existsByFollowerAndFollowingAndStatus(currentuser,usertofollow, Follow.Status.PENDING)){
           throw new BadFollowRequestException("request already sent");
       }
        Follow follow = new Follow(currentuser, usertofollow);

        notification notification= new notification(currentuser,usertofollow,
                com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOW);
        RelationshipStatus status;
        if (profileRepo.existsByUserAndIsprivateFalse(usertofollow)) {
            follow.setStatus(Follow.Status.ACCEPTED);
            follow.setAccepteddate(Instant.now());
            log.info("publishing follow event for "+usertofollow.getUsername());
            eventPublisher.publishEvent(notification);
            eventPublisher.publishEvent(new followAdded(follow));
            status=RelationshipStatus.FOLLOWING;
        } else {
            follow.setStatus(Follow.Status.PENDING);
            log.info("publishing follow request event for "+usertofollow.getUsername());
            notification.setType(com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOW_REQUESTED);
            eventPublisher.publishEvent(notification);
            status=RelationshipStatus.FOLLOW_REQUESTED;
        }

        followRepo.save(follow);
        return profileDetails.builder().userId(userId).status(status).build();
    }

    // this method works for both pending and accepted followings
    @CheckUserExistence
    public void UnFollow(String userId) {
        User currentUser = authenticatedUserService.getcurrentuser(false);
        User targetUser=new User(userId);
        Follow follow = followRepo.findByFollowerAndFollowing( currentUser,targetUser).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));
        followRepo.delete(follow);
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            eventPublisher.publishEvent(new followRemoved(follow));
        }
    }

    // this method works for both pending and accepted followers
    @CheckUserExistence
    public void removefollower(String userId) {
        User currentUser= authenticatedUserService.getcurrentuser(false);
        User targetUser=new User(userId);
        Follow follow = followRepo.findByFollowerAndFollowing(targetUser,currentUser).
                orElseThrow(()->new NoRelationShipException("No relationship with user found"));
        followRepo.delete(follow);
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            eventPublisher.publishEvent(new followRemoved(follow));
        }else{
            eventPublisher.publishEvent(new notification(currentUser,targetUser, notification.notificationType.FOLLOWING_REJECTED));
        }
    }
}


