package com.example.whatsappclone.Services;

import com.example.whatsappclone.DTO.serverToclient.user;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Events.notification;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepo followRepo;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final UsersManagmentService usersManagment;
    private final BlocksRepo blocksRepo;
    private final CachService cachService;
    private final ApplicationEventPublisher eventPublisher;
    private final Logger logger= LoggerFactory.getLogger(FollowService.class);

    public user Follow(String useruuid) {
        User currentuser = usersManagment.getcurrentuser();
        if (currentuser.getUuid().equals(useruuid)) {
            throw new BadFollowRequestException("you cant follow yourself");
        }
        User usertofollow = userRepo.findById(useruuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        followRepo.findByFollowerAndFollowingAndStatus(currentuser, usertofollow, Follow.Status.ACCEPTED).
                ifPresent(follow -> {
                    throw new BadFollowRequestException("Already followed");
                });
        blocksRepo.findByBlockedAndBlocker(currentuser, usertofollow).ifPresent(blocks -> {
            throw new BadFollowRequestException("User has blocked you");
        });
        blocksRepo.findByBlockedAndBlocker(usertofollow, currentuser).ifPresent(blocks -> {
            throw new BadFollowRequestException("You have blocked this user");
        });
        followRepo.findByFollowerAndFollowingAndStatus(currentuser, usertofollow, Follow.Status.PENDING).
                ifPresent(follow -> {
                    throw new BadFollowRequestException("request already sent");
                });
        Follow follow = new Follow(currentuser, usertofollow);
        Profile profile=profileRepo.findByUser(usertofollow).get();
        if (!profile.isIsprivate()) {
            follow.setStatus(Follow.Status.ACCEPTED);
            follow.setAccepteddate(Instant.now());
            cachService.addfollower(usertofollow,follow);
            cachService.addfollowing(currentuser,follow);
            logger.info("publishing follow event for "+usertofollow.getUsername());
            eventPublisher.publishEvent(new notification(currentuser,usertofollow,
                    notification.notificationType.FOLLOW,follow.getUuid()));
        } else {
            follow.setStatus(Follow.Status.PENDING);
            logger.info("publishing follow request event for "+usertofollow.getUsername());
            eventPublisher.publishEvent(new notification(currentuser,usertofollow,
                    notification.notificationType.FOLLOW_REQUESTED,follow.getUuid()));
        }
        followRepo.save(follow);
        return new user(useruuid,usertofollow.getUsername(),profile.getPublicavatarurl(),follow.getUuid());
    }

    public void UnFollow(String followuuid) {
        User currentuser = usersManagment.getcurrentuser();
        Follow follow = followRepo.findByUuidAndFollower(followuuid, currentuser).orElseThrow();
        User userfollowing=follow.getFollowing();
        if (follow.getStatus().equals(Follow.Status.PENDING)) {
            throw new BadFollowRequestException("you are not following this user try to unsend the request");
        }
        followRepo.delete(follow);
        cachService.removefollowing(currentuser,follow);
        cachService.removefollower(userfollowing,follow);
    }
    public void removefollower(String followuuid) {
        User currentuser = usersManagment.getcurrentuser();
        Follow follow = followRepo.
                findByUuidAndFollowing(followuuid,currentuser).
                orElseThrow(()->new BadFollowRequestException("bad request"));
        User userfollower=follow.getFollower();
        if (follow.getStatus() == Follow.Status.PENDING) {
            throw new BadFollowRequestException("user not in followers try to reject the request");
        }
        followRepo.delete(follow);
        cachService.removefollower(currentuser,follow);
        cachService.removefollowing(userfollower,follow);
    }
}


