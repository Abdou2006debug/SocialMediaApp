package com.example.whatsappclone.Shared;

import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Shared.Exceptions.FollowListNotVisibleException;
import com.example.whatsappclone.SocialGraph.application.FollowQueryHelper;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import com.example.whatsappclone.User.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisibilityPolicy {

    private final BlocksRepo blocksRepo;
    private final ProfileRepo profileRepo;
    private final FollowRepo followRepo;


    public boolean isAllowed(User currentUser, User requestedUser){
        boolean isblocked=blocksRepo.existsByBlockerAndBlocked(requestedUser,currentUser);
        if(isblocked){
            return false;
        }
        boolean hasblocked= blocksRepo.existsByBlockerAndBlocked(currentUser,requestedUser);
        if (hasblocked) {
            return false;
        }

        if(!profileRepo.existsByUserAndIsprivateFalse(requestedUser)){
            if(!followRepo.existsByFollowerAndFollowingAndStatus(currentUser,requestedUser, Follow.Status.ACCEPTED)){
                return false;
            }
        }
        return true;

    }


}
