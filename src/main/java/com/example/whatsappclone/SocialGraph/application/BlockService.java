package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Identity.persistence.UserRepo;
import com.example.whatsappclone.Shared.CheckUserExistence;
import com.example.whatsappclone.Shared.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Shared.Exceptions.UserNotFoundException;
import com.example.whatsappclone.SocialGraph.domain.Blocks;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {
    private final BlocksRepo blocksRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserRepo userRepo;
    private final FollowRepo followRepo;
    private final FollowRequestService followRequestService;
    private final FollowService followService;

    @CheckUserExistence
    public void block(String useruuid) {
        User currentuser = authenticatedUserService.getcurrentuser(false);
        User requesteduser=new User(useruuid);
        if(currentuser.getUuid().equals(useruuid)){throw new BadFollowRequestException("you cant block yourself");}
        boolean isalreadyblocked= blocksRepo.
                existsByBlockerAndBlocked(currentuser,requesteduser);
        if(isalreadyblocked){
            throw new BadFollowRequestException("you have already blocked this user");
        }
        Blocks block = new Blocks(currentuser,requesteduser);
        followRepo.findByFollowerAndFollowing(currentuser,requesteduser).ifPresent(f -> {
            if (f.getStatus() == Follow.Status.ACCEPTED) {
                followService.UnFollow(f.getUuid());
                return;
            }
            followRequestService.unsendFollowingRequest(f.getUuid());
        });
        followRepo.
                findByFollowerAndFollowing(requesteduser, currentuser).ifPresent(follow -> {
                    if (follow.getStatus() == Follow.Status.ACCEPTED) {
                        followService.removefollower(follow.getUuid());
                        return;
                    }
                    followRequestService.rejectFollow(follow.getUuid());
                });
        blocksRepo.save(block);
    }


    @CheckUserExistence
    public void unblock(String userId) {
        User currentuser = authenticatedUserService.getcurrentuser(false);
        User usertounblock = new User(userId);
        Blocks block = blocksRepo.
                findByBlockedAndBlocker(usertounblock, currentuser).
                orElseThrow(() -> new BadFollowRequestException("you have not blocked this user"));
        blocksRepo.delete(block);
    }
}
