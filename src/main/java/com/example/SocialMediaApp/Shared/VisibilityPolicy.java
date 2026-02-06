package com.example.SocialMediaApp.Shared;

import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Aspect
public class VisibilityPolicy {

    private final BlocksRepo blocksRepo;
    private final ProfileRepo profileRepo;
    private final FollowRepo followRepo;
    private final AuthenticatedUserService authenticatedUserService;


    public boolean isAllowed(User currentUser,User requestedUser){
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

