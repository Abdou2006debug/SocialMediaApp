package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Events.notification;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowRequestService {
    private final FollowRepo followRepo;
    private final UserFollowViewHelper followHelperService;
    private final CacheWriterService cachService;
    private final Logger logger= LoggerFactory.getLogger(FollowRequestService.class);
    private final ApplicationEventPublisher eventPublisher;
    private final UserQueryService userQueryService;
    private final UserRepo userRepo;
    public void acceptfollow(String userId) {
        if(!userRepo.existsById(userId)){
            throw new UserNotFoundException("user not found");
        }
        User currentuser =userQueryService.getcurrentuser(false);
        User targetUser=new User(userId);
        Follow followrequest = followRepo.
                findByFollowingAndFollowerAndStatus(currentuser,targetUser, Follow.Status.PENDING).
                orElseThrow(()->new BadFollowRequestException("couldn't perform accept follow action"));
        followrequest.setStatus(Follow.Status.ACCEPTED);
        followrequest.setAccepteddate(Instant.now());
        followRepo.save(followrequest);
        logger.info("publishing following accepted event to "+targetUser.getUsername());
        eventPublisher.publishEvent(new notification(currentuser,targetUser,
                notification.notificationType.FOLLOWING_ACCEPTED));
    }

    public void rejectfollow(String userId) {
        if(!userRepo.existsById(userId)){
            throw new UserNotFoundException("user not found");
        }
        User currentUser = userQueryService.getcurrentuser(false);
        User targetUser=new User(userId);
        Follow follow = followRepo.findByFollowingAndFollowerAndStatus(currentUser,targetUser, Follow.Status.PENDING).
                orElseThrow(()->new BadFollowRequestException("couldn't perform reject follow action"));
        followRepo.delete(follow);
        logger.info("publishing following rejected event to "+targetUser.getUsername());
        eventPublisher.publishEvent(new notification(currentUser,targetUser,
                notification.notificationType.FOLLOWING_REJECTED));
    }

    public List<profileSummary> listCurrentUserFollowRequests(int page) {
        User currentUser=userQueryService.getcurrentuser(false);
        return followHelperService.
                listCurrentUserPendingFollows(currentUser.getUuid(), UserFollowViewHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowingRequests(int page) {
        User currentUser=userQueryService.getcurrentuser(false);
        return followHelperService.
                listCurrentUserPendingFollows(currentUser.getUuid(), UserFollowViewHelper.Position.FOLLOWINGS,page);

    }
    public void unsendfollowingrequest(String userId){
        if(!userRepo.existsById(userId)){
            throw new UserNotFoundException("user not found");
        }
    User currentUser=userQueryService.getcurrentuser(false);
        User targetUser=new User(userId);
    Follow followrequest = followRepo.findByFollowingAndFollowerAndStatus().
            orElseThrow(()->new BadFollowRequestException("bad request"));
        if (followrequest.getStatus() == Follow.Status.ACCEPTED) {
        throw new BadFollowRequestException("you already follow this user");
    }
        followRepo.delete(followrequest);
}


}
