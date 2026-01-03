package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowQueryService {
private final UserFollowViewHelper followViewHelper;
private final UserRepo userRepo;
private final UserQueryService userQueryService;

    public List<profileSummary> listCurrentUserFollowers(int page) {
        User currentuser =userQueryService.getcurrentuser(false);
        return  followViewHelper.listCurrentUserFollows(currentuser.getUuid(), UserFollowViewHelper.Position.FOLLOWERS,page);
    }

    public List<profileSummary> listCurrentUserFollowings(int page) {
        User currentuser =userQueryService.getcurrentuser(false);
        return  followViewHelper.listCurrentUserFollows(currentuser.getUuid(), UserFollowViewHelper.Position.FOLLOWINGS,page);
    }

    public List<profileSummary> listUserFollowers(int page) {
     //   return followViewHelper.ListMyFollows_Accepted(UserFollowViewHelper.Position.FOLLOWERS,page,userQueryService.getcurrentuser());
    }


    public List<profileSummary> listUserFollowers(String userId, int page){
        if(!userRepo.existsById(userId)){
            throw  new UserNotFoundException("user not found");
        }
        User currentuser=userQueryService.getcurrentuser(false);
        User requesteduser=new User(userId);
        followViewHelper.canViewUserFollows(currentuser,requesteduser, UserFollowViewHelper.Position.FOLLOWERS);
        return followViewHelper.listUserFollows(currentuser.getUuid(),requesteduser.getUuid(), UserFollowViewHelper.Position.FOLLOWERS,page);
    }
    public List<profileSummary> listUserFollowing(String userId,int page){
        if(!userRepo.existsById(userId)){
            throw  new UserNotFoundException("user not found");
        }
        User currentuser=userQueryService.getcurrentuser(false);
        User requesteduser=new User(userId);
        followViewHelper.canViewUserFollows(currentuser,requesteduser, UserFollowViewHelper.Position.FOLLOWINGS);
        return followViewHelper.listUserFollows(currentuser.getUuid(),requesteduser.getUuid(), UserFollowViewHelper.Position.FOLLOWINGS,page);

    }
}
