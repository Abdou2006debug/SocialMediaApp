package com.example.whatsappclone.Services;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {
    private final BlocksRepo blocksRepo;
    private final UsersManagmentService usersManagment;
    private final UserRepo userRepo;
    private final FollowRepo followRepo;
    private final FollowRequestService followRequestService;
    private final FollowService followService;

    public void block(String useruuid) {
        User currentuser = usersManagment.getcurrentuser();
        if(currentuser.getUuid().equals(useruuid)){throw new BadFollowRequestException("you cant block yourself");}
        User usertoblock = userRepo.
                findById(useruuid).orElseThrow(()->new UserNotFoundException("user not found"));
        boolean isalreadyblocked= blocksRepo.
                existsByBlockedAndBlocker(usertoblock, currentuser);
        if(isalreadyblocked){
            throw new BadFollowRequestException("you have already blocked this user");
        }
        Blocks block = new Blocks(currentuser, usertoblock);
        followRepo.findByFollowerAndFollowing(currentuser, usertoblock).ifPresent(f -> {
            if (f.getStatus() == Follow.Status.ACCEPTED) {
                followService.UnFollow(f.getUuid());
                return;
            }
            followRequestService.unsendfollowingrequest(f.getUuid());
        });
        followRepo.
                findByFollowerAndFollowing(usertoblock, currentuser).ifPresent(f -> {
                    if (f.getStatus() == Follow.Status.ACCEPTED) {
                        followService.removefollower(f.getUuid());
                        return;
                    }
                    followRequestService.rejectfollow(f.getUuid());
                });
        blocksRepo.save(block);
    }



    public void unblock(String useruuid) {
        User currentuser = usersManagment.getcurrentuser();
        User usertounblock = userRepo.findById(useruuid).
                orElseThrow(()->new UserNotFoundException("user not found"));
        Blocks block = blocksRepo.
                findByBlockedAndBlocker(usertounblock, currentuser).
                orElseThrow(() -> new BadFollowRequestException("you have not blocked this user"));
        blocksRepo.delete(block);
    }

}
