package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import com.example.whatsappclone.Services.UserManagmentServices.UsersAccountManagmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {
    private final BlocksRepo blocksRepo;
    private final UsersAccountManagmentService usersManagment;
    private final UserRepo userRepo;
    private final FollowRepo followRepo;
    private final FollowRequestService followRequestService;
    private final FollowService followService;
    private final UserQueryService userQueryService;
    public void block(String useruuid) {
        User currentuser = userQueryService.getcurrentuser(false);
        User requesteduser=new User(useruuid);
        if(!userRepo.existsById(useruuid)){throw new UserNotFoundException("user not found");}
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
            followRequestService.unsendfollowingrequest(f.getUuid());
        });
        followRepo.
                findByFollowerAndFollowing(requesteduser, currentuser).ifPresent(follow -> {
                    if (follow.getStatus() == Follow.Status.ACCEPTED) {
                        followService.removefollower(follow.getUuid());
                        return;
                    }
                    followRequestService.rejectfollow(follow.getUuid());
                });
        blocksRepo.save(block);
    }



    public void unblock(String useruuid) {
        User currentuser = userQueryService.getcurrentuser(false);
        User usertounblock = userRepo.findById(useruuid).
                orElseThrow(()->new UserNotFoundException("user not found"));
        Blocks block = blocksRepo.
                findByBlockedAndBlocker(usertounblock, currentuser).
                orElseThrow(() -> new BadFollowRequestException("you have not blocked this user"));
        blocksRepo.delete(block);
    }
}
