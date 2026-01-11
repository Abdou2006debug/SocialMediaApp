package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Shared.Exceptions.FollowListNotVisibleException;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FollowVisibilityPolicy {
    private final BlocksRepo blocksRepo;
    private final ProfileRepo profileRepo;
    private final FollowRepo followRepo;

    public void canViewUserFollows(User currentUser, User requestedUser, FollowQueryHelper.Position position){
        boolean isblocked=blocksRepo.existsByBlockerAndBlocked(requestedUser,currentUser);
        if(isblocked){
            throw  new FollowListNotVisibleException(String.format("%s list for user is not visible.",position.name()));
        }
        boolean hasblocked= blocksRepo.existsByBlockerAndBlocked(currentUser,requestedUser);
        if (hasblocked) {
            throw new FollowListNotVisibleException(String.format("%s list for user is not visible.",position.name()));
        }

        if(!profileRepo.existsByUserAndIsprivateFalse(requestedUser)){
            if(!followRepo.existsByFollowerAndFollowingAndStatus(currentUser,requestedUser, Follow.Status.ACCEPTED)){
                throw new FollowListNotVisibleException(String.format("%s list for user is not visible: the profile is private.",position.name()));
            };
        }

    }
}
