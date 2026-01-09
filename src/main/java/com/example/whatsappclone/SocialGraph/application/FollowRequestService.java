package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.Events.notification;
import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Identity.persistence.UserRepo;
import com.example.whatsappclone.Shared.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Shared.Exceptions.NoRelationShipException;
import com.example.whatsappclone.Shared.Exceptions.UserNotFoundException;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.domain.events.followAdded;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
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
    private final UserRepo userRepo;

    public void acceptFollow(String userId) {
        if(!userRepo.existsById(userId)){
            throw new UserNotFoundException("user not found");
        }
        User currentuser =authenticatedUserService.getcurrentuser(false);
        User targetUser=new User(userId);
        Follow followRequest = followRepo.
                findByFollowerAndFollowing(targetUser,currentuser).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));
        if(followRequest.getStatus()== Follow.Status.ACCEPTED){
            throw new BadFollowRequestException("couldn't perform accept follow action on this user");
        }
        followRequest.setStatus(Follow.Status.ACCEPTED);
        followRequest.setAccepteddate(Instant.now());
        followRepo.save(followRequest);
        logger.info("publishing following accepted event to "+targetUser.getUsername());
        eventPublisher.publishEvent(new notification(currentuser,targetUser,
                notification.notificationType.FOLLOWING_ACCEPTED));
        eventPublisher.publishEvent(new followAdded(followRequest));
    }

    public void rejectFollow(String userId) {
        if(!userRepo.existsById(userId)){
            throw new UserNotFoundException("user not found");
        }
        User currentUser = authenticatedUserService.getcurrentuser(false);
        User targetUser=new User(userId);
        Follow follow = followRepo.findByFollowerAndFollowing(targetUser,currentUser).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            throw new BadFollowRequestException("couldn't perform reject follow action on this user");
        }
        followRepo.delete(follow);
        logger.info("publishing following rejected event to "+targetUser.getUsername());
        eventPublisher.publishEvent(new notification(currentUser,targetUser,
                notification.notificationType.FOLLOWING_REJECTED));
    }


    public void unsendFollowingRequest(String userId){
        if(!userRepo.existsById(userId)){
            throw new UserNotFoundException("user not found");
        }
        User currentUser=authenticatedUserService.getcurrentuser(false);
        User targetUser=new User(userId);
    Follow followingRequest = followRepo.findByFollowerAndFollowing(currentUser,targetUser).
            orElseThrow(()->new NoRelationShipException("No relation with user found"));
        if (followingRequest.getStatus() == Follow.Status.ACCEPTED) {
        throw new BadFollowRequestException("couldn't perform unsend follow request on this user");
    }
        followRepo.delete(followingRequest);
}


}
