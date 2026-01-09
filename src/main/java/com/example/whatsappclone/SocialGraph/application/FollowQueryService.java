package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Identity.persistence.UserRepo;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Shared.Exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowQueryService {
    private final UserFollowViewHelper followViewHelper;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;

    public List<profileSummary> listCurrentUserFollowers(int page) {
        User currentuser =authenticatedUserService.getcurrentuser(false);
        return  followViewHelper.listCurrentUserFollows(currentuser.getUuid(), UserFollowViewHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowings(int page) {
        User currentuser = authenticatedUserService.getcurrentuser(false);
        return  followViewHelper.listCurrentUserFollows(currentuser.getUuid(), UserFollowViewHelper.Position.FOLLOWINGS,page);
    }

    public List<profileSummary> listCurrentUserFollowRequests(int page) {
        User currentUser= authenticatedUserService.getcurrentuser(false);
        return followViewHelper.
                listCurrentUserPendingFollows(currentUser.getUuid(), UserFollowViewHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowingRequests(int page) {
        User currentUser= authenticatedUserService.getcurrentuser(false);
        return followViewHelper.
                listCurrentUserPendingFollows(currentUser.getUuid(), UserFollowViewHelper.Position.FOLLOWINGS,page);

    }

    public List<profileSummary> listUserFollowers(String userId, int page){
        if(!userRepo.existsById(userId)){
            throw  new UserNotFoundException("user not found");
        }
        User currentuser= authenticatedUserService.getcurrentuser(false);
        User requesteduser=new User(userId);
        followViewHelper.canViewUserFollows(currentuser,requesteduser, UserFollowViewHelper.Position.FOLLOWERS);
        return followViewHelper.listUserFollows(currentuser.getUuid(),requesteduser.getUuid(), UserFollowViewHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listUserFollowing(String userId,int page){
        if(!userRepo.existsById(userId)){
            throw  new UserNotFoundException("user not found");
        }
        User currentUser= authenticatedUserService.getcurrentuser(false);
        User targetUser=new User(userId);
        followViewHelper.canViewUserFollows(currentUser,targetUser, UserFollowViewHelper.Position.FOLLOWINGS);
        return followViewHelper.listUserFollows(currentUser.getUuid(),targetUser.getUuid(), UserFollowViewHelper.Position.FOLLOWINGS,page);

    }

}
