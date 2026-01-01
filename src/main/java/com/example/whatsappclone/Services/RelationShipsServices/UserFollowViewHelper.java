package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.DTO.serverToclient.RelationshipStatus;
import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.FollowListNotVisibleException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.CacheServices.CacheQueryService;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import com.example.whatsappclone.Services.UserManagmentServices.Usermapper;
import com.example.whatsappclone.Services.UserManagmentServices.UsersAccountManagmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFollowViewHelper {

private final CacheWriterService cacheWriterService;
private final CacheQueryService cacheQueryService;
private final UserQueryService userQueryService;
private final FollowRepo followRepo;
private final Usermapper usermapper;
private final BlocksRepo blocksRepo;
    public enum Position {FOLLOWERS, FOLLOWINGS}



    public List<profileSummary> ListFollow_Accepted(String userId,Position position, int page,boolean iscurrent){
     List<profileSummary> profileSummaries=getProfileSummaries(userId,position,page,iscurrent);
     return profileSummaries.stream().map(profile -> {
        profileSummary profileSummary= buildProfileSummary(profile.getUserId());
         if(iscurrent){
             resolveCurrentUserFollowRelationShips(profileSummary,userId,position);
         }else{
             resolveUserFollowRelationShips(profileSummary,userId);
         }
         return profileSummary;
     }).toList();
       }

       private void resolveCurrentUserFollowRelationShips(profileSummary profile,String userId,Position position){
               if(position==Position.FOLLOWINGS){
                   profile.setStatus(RelationshipStatus.FOLLOWING);
                   return;
               }
               if(!followRepo.existsByFollowerAndFollowing(new User(userId),new User(profile.getUserId()))){
                   profile.setStatus(RelationshipStatus.NOT_FOLLOWING);
                   return;
               }
              if(followRepo.existsByFollowerAndFollowingAndStatus(new User(userId),new User(profile.getUserId()), Follow.Status.PENDING)){
                  profile.setStatus(RelationshipStatus.FOLLOW_REQUESTED);
                  return;
              }
                  profile.setStatus(RelationshipStatus.FOLLOWING);
       }
       private void resolveUserFollowRelationShips(profileSummary profile,String userId){

       }
       private profileSummary buildProfileSummary(String userId){
        ProfileInfo profileInfocache = cacheQueryService.getProfileInfo(userId).orElseGet(()->{
                Profile profile=userQueryService.getuserprofile(new User(userId),false);
           return cacheWriterService.cacheProfileInfo(profile);
        });
          return usermapper.toSummary(profileInfocache);
}
        private List<profileSummary> getProfileSummaries(String userId,Position position,int page,boolean iscurrent){
           if(position==Position.FOLLOWERS){
               return cacheQueryService.getuserCachedFollowers(userId,page,iscurrent).orElseGet(()->cacheWriterService.cachUserFollowers(userId,page));
           }
            return cacheQueryService.getusercachedfollowings(userId,page,iscurrent).orElseGet(()->cacheWriterService.cachUserFollowings(userId,page));
}

        public void canViewUserFollows(User currentUser,User requestedUser,Position position){
            boolean isblocked=blocksRepo.existsByBlockerAndBlocked(requestedUser,currentUser);
            if(isblocked){
                throw  new FollowListNotVisibleException(String.format("%s list for user is not visible.",position.name()));
            }
            boolean hasblocked= blocksRepo.existsByBlockerAndBlocked(currentUser,requestedUser);
            if (hasblocked) {
                throw new FollowListNotVisibleException(String.format("%s list for user is not visible.",position.name()));
            }

            Profile profile=userQueryService.getuserprofile(requestedUser,false);
            if(profile.isIsprivate()){
                if(!followRepo.existsByFollowerAndFollowingAndStatus(currentUser,requestedUser, Follow.Status.ACCEPTED)){
                    throw new FollowListNotVisibleException(String.format("%s list for user is not visible: the profile is private.",position.name()));
                };
            }

        }
}

