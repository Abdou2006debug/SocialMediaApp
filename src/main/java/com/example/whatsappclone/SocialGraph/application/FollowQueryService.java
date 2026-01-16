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

    private final FollowQueryHelper followViewResolver;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;

    public List<profileSummary> listCurrentUserFollowers(int page) {
        User currentuser =authenticatedUserService.getcurrentuser(false);
        return  followViewResolver.listCurrentUserFollows(currentuser.getUuid(), FollowQueryHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowings(int page) {
        User currentuser = authenticatedUserService.getcurrentuser(false);
        return  followViewResolver.listCurrentUserFollows(currentuser.getUuid(), FollowQueryHelper.Position.FOLLOWINGS,page);
    }

    public List<profileSummary> listCurrentUserFollowRequests(int page) {
        User currentUser= authenticatedUserService.getcurrentuser(false);
        return followViewResolver.
                listCurrentUserPendingFollows(currentUser.getUuid(), FollowQueryHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowingRequests(int page) {
        User currentUser= authenticatedUserService.getcurrentuser(false);
        return followViewResolver.
                listCurrentUserPendingFollows(currentUser.getUuid(), FollowQueryHelper.Position.FOLLOWINGS,page);

    }

    @CheckUserExistence
    public List<profileSummary> listUserFollowers(String userId, int page){
        User currentUser= authenticatedUserService.getcurrentuser(false);
        User targetUser =new User(userId);
       boolean isAllowed= visibilityPolicy.isAllowed(currentUser, targetUser);
       if(!isAllowed){
           throw new FollowListNotVisibleException("followers list for user is not visible: the profile is private.");
       }
        return followViewResolver.listUserFollows(currentUser.getUuid(), targetUser.getUuid(), FollowQueryHelper.Position.FOLLOWERS,page);
    }

    @CheckUserExistence
    public List<profileSummary> listUserFollowing(String userId,int page){
        User currentUser= authenticatedUserService.getcurrentuser(false);
        User targetUser=new User(userId);
        boolean isAllowed= visibilityPolicy.isAllowed(currentUser,targetUser);
        if(!isAllowed){
            throw new FollowListNotVisibleException("followings list for user is not visible: the profile is private.");
        }
        return followViewResolver.listUserFollows(currentUser.getUuid(),targetUser.getUuid(), FollowQueryHelper.Position.FOLLOWINGS,page);

    }

}
