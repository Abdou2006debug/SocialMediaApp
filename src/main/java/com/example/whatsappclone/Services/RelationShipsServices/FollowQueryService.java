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




      //  return followViewHelper.ListOtherFollows(position,page,requesteduser).stream().peek(follows-> {
        //    follows.setFollowuuid(null);
          //  String status=null;
         //   String followeruuid= follows.getUseruuid();
          //  boolean isfolloweraccepted = followRepo.
           //         existsByFollowerAndFollowing_UuidAndStatus(currentuser,followeruuid, Follow.Status.ACCEPTED);
           // if(isfolloweraccepted){
            //    status="following";
            //}else{
             //   boolean isfollowerpending=followRepo.
              //          existsByFollowerAndFollowing_UuidAndStatus(currentuser,followeruuid, Follow.Status.PENDING);
               // if(isfollowerpending){
                //    status="sent";
               // }
           // }
           // boolean i=followRepo.existsByFollowerAndFollowingUuid(currentuser, followeruuid);
           // boolean isfollowingaccepted=followRepo.
            //        existsByFollower_UuidAndFollowingAndStatus(followeruuid,currentuser, Follow.Status.ACCEPTED);
            //if(isfollowingaccepted&&!i){
            //    status="follow back";
            //}
            //follows.setStatus(status);
        //}).toList();
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
