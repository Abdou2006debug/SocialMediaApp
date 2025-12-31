package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowQueryService {
private final FollowUtill followHelperService;
private final CacheWriterService cachService;
private final FollowRepo followRepo;
private final BlocksRepo blocksRepo;
private final UserRepo userRepo;
private final UserQueryService userQueryService;
    public List<user> ListMyfollowers(int page) {
        return followHelperService.
               ListMyFollows_Accepted(FollowUtill.Position.FOLLOWER,
                        page,userQueryService.getcurrentuser());
    }



    public List<user> listMyfollowings(int page) {
        return followHelperService.
                ListMyFollows_Accepted(FollowUtill.Position.FOLLOWING,page
                        ,userQueryService.getcurrentuser());
    }


    public List<user> getUserFollow(String useruuid, int page, FollowUtill.Position position){
        User currentuser=userQueryService.getcurrentuser(false);
        User requesteduser;
        requesteduser=cachService.getUserbyId(useruuid);
        if (requesteduser == null) {
            requesteduser= userRepo.findById(useruuid).
                    orElseThrow(() -> new UserNotFoundException("user not found"));
        }
        boolean isblocked=blocksRepo.existsByBlockerAndBlocked(requesteduser,currentuser);
        boolean hasblocked= blocksRepo.existsByBlockerAndBlocked(currentuser,requesteduser);
        if (hasblocked) {
            throw new BadFollowRequestException("you cant see his followings because you blocked him");
        }
        if(isblocked){
            throw new BadFollowRequestException("you cant see his followings because user has blocked you");
        }
        Profile profile=userQueryService.getuserprofile(requesteduser,false);
        if(profile.isIsprivate()){
            if(!followRepo.existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.ACCEPTED)){
                throw new BadFollowRequestException("this user has private access");
            };
        }
        return followHelperService.ListOtherFollows(position,page,requesteduser).stream().peek(follows-> {
            follows.setFollowuuid(null);
            String status=null;
            String followeruuid= follows.getUseruuid();
            boolean isfolloweraccepted = followRepo.
                    existsByFollowerAndFollowing_UuidAndStatus(currentuser,followeruuid, Follow.Status.ACCEPTED);
            if(isfolloweraccepted){
                status="following";
            }else{
                boolean isfollowerpending=followRepo.
                        existsByFollowerAndFollowing_UuidAndStatus(currentuser,followeruuid, Follow.Status.PENDING);
                if(isfollowerpending){
                    status="sent";
                }
            }
            boolean i=followRepo.existsByFollowerAndFollowingUuid(currentuser, followeruuid);
            boolean isfollowingaccepted=followRepo.
                    existsByFollower_UuidAndFollowingAndStatus(followeruuid,currentuser, Follow.Status.ACCEPTED);
            if(isfollowingaccepted&&!i){
                status="follow back";
            }
            follows.setStatus(status);
        }).toList();
    }
}
