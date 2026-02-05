package com.example.whatsappclone.SocialGraph.application;

import com.example.whatsappclone.Shared.Exceptions.FollowListNotVisibleException;
import com.example.whatsappclone.Shared.VisibilityPolicy;
import com.example.whatsappclone.User.application.AuthenticatedUserService;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Shared.CheckUserExistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowQueryHelper followQueryHelper;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;

    public List<profileSummary> listCurrentUserFollowers(int page) {
        User currentuser =authenticatedUserService.getcurrentuser();
        return  followQueryHelper.listCurrentUserFollows(currentuser.getId(), FollowQueryHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowings(int page) {
        User currentuser = authenticatedUserService.getcurrentuser();
        return  followQueryHelper.listCurrentUserFollows(currentuser.getId(), FollowQueryHelper.Position.FOLLOWINGS,page);
    }

    public List<profileSummary> listCurrentUserFollowRequests(int page) {
        User currentUser= authenticatedUserService.getcurrentuser();
        return followQueryHelper.
                listCurrentUserPendingFollows(currentUser.getId(),FollowQueryHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowingRequests(int page) {
        User currentUser= authenticatedUserService.getcurrentuser();
        return followQueryHelper.
                listCurrentUserPendingFollows(currentUser.getId(), FollowQueryHelper.Position.FOLLOWINGS,page);

    }

    @CheckUserExistence
    public List<profileSummary> listUserFollowers(String userId, int page){
        User currentUser= authenticatedUserService.getcurrentuser();
        User targetUser =new User(userId);
       boolean isAllowed= visibilityPolicy.isAllowed(currentUser, targetUser);
       if(!isAllowed){
           throw new FollowListNotVisibleException("followers list for user is not visible.");
       }
        return followQueryHelper.listUserFollows(currentUser.getId(), targetUser.getId(), FollowQueryHelper.Position.FOLLOWERS,page);
    }

    @CheckUserExistence
    public List<profileSummary> listUserFollowing(String userId,int page){
        User currentUser= authenticatedUserService.getcurrentuser();
        User targetUser=new User(userId);
        boolean isAllowed= visibilityPolicy.isAllowed(currentUser,targetUser);
        if(!isAllowed){
            throw new FollowListNotVisibleException("followings list for user is not visible.");
        }
        return followQueryHelper.listUserFollows(currentUser.getId(),userId, FollowQueryHelper.Position.FOLLOWINGS,page);

    }

}
