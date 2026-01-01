package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Events.notification;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Repositries.FollowRepo;
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
    public void acceptfollow(String followuuid) {
        User currentuser =userQueryService.getcurrentuser(false);
        Follow followrequest = followRepo.
                findByUuidAndFollowing(followuuid,currentuser).orElseThrow(()->new BadFollowRequestException("bad request"));
        User userfollower=followrequest.getFollower();
        if (followrequest.getStatus() == Follow.Status.ACCEPTED) {
            throw new BadFollowRequestException("user already follow you");
        }
        followrequest.setStatus(Follow.Status.ACCEPTED);
        followrequest.setAccepteddate(Instant.now());
        followRepo.save(followrequest);
        logger.info("publishing following accepted event to "+userfollower.getUsername());
        eventPublisher.publishEvent(new notification(currentuser,userfollower,
                notification.notificationType.FOLLOWING_ACCEPTED));
        cachService.addfollower(currentuser,followrequest);
        cachService.addfollowing(userfollower,followrequest);
    }

    public void rejectfollow(String followuuid) {
        User currentuser = userQueryService.getcurrentuser(false);
        Follow follow = followRepo.findByUuidAndFollowing(followuuid, currentuser).orElseThrow();
        User userfollower=follow.getFollower();
        if (follow.getStatus() == Follow.Status.ACCEPTED) {
            throw new BadFollowRequestException("User Already in followers");
        }
        followRepo.delete(follow);
        logger.info("publishing following rejected event to "+userfollower.getUsername());
        eventPublisher.publishEvent(new notification(currentuser,userfollower,
                notification.notificationType.FOLLOWING_REJECTED));
    }

    public List<user> ListMyFollowRequests(int page) {
        return followHelperService.
                ListMyFollows_Pending(UserFollowViewHelper.Position.FOLLOWER,
                        page,userQueryService.getcurrentuser());
    }
    public List<user> ListMyFollowingRequests(int page) {
        return followHelperService
                .ListMyFollows_Pending(UserFollowViewHelper.Position.FOLLOWING,page
                        ,userQueryService.getcurrentuser(false));
    }
    public void unsendfollowingrequest(String followuuid){
    User currentuser=userQueryService.getcurrentuser(false);
    Follow followrequest = followRepo.findByUuidAndFollower(followuuid,currentuser).
            orElseThrow(()->new BadFollowRequestException("bad request"));
        if (followrequest.getStatus() == Follow.Status.ACCEPTED) {
        throw new BadFollowRequestException("you already follow this user");
    }
        followRepo.delete(followrequest);
}


}
