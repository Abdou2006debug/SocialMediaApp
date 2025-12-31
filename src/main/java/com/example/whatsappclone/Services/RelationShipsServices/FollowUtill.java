package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.DTO.serverToclient.RelationshipStatus;
import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.lang.reflect.InaccessibleObjectException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowUtill {
private final ApplicationEventPublisher applicationEventPublisher;
private final CacheWriterService cacheWriterService;
private final CacheQueryService cacheQueryService;
private final UserRepo userRepo;
private final ProfileRepo profileRepo;
private final UsersAccountManagmentService usersManagment;
private final UserQueryService userQueryService;
private final FollowRepo followRepo;
private final Usermapper usermapper;
    public enum Position {FOLLOWER, FOLLOWING}


    public List<profileSummary> ListFollow_Accepted(String userId,Position position, int page,boolean iscurrent){
      Optional<List<profileSummary>>  profileSummaries= position==Position.FOLLOWER?
              cacheQueryService.getuserCachedFollowers(userId,page,iscurrent):cacheQueryService.getusercachedfollowings(userId,page,iscurrent);
      if(profileSummaries.isEmpty()){
          profileSummaries= Optional.ofNullable(position == Position.FOLLOWER ?
                  cacheWriterService.cachUserFollowers(userId, page) : cacheWriterService.cachUserFollowings(userId, page));
      }
      if(profileSummaries.isEmpty()){
          throw new InaccessibleObjectException("could not complete action please try later");
      }
     return profileSummaries.get().stream().map(profile -> {
       return populateProfileSummary(profile.getUserId());
        }).toList();
       }

       private void populateRelationships(profileSummary profile,String userId,Position position,boolean iscurrent){
           if(iscurrent){
               if(position==Position.FOLLOWING){
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
                  return;
       }
       }
       private profileSummary populateProfileSummary(String userId){
        Optional<ProfileInfo> profileInfo= cacheQueryService.getProfileInfo(userId);
        if(profileInfo.isEmpty()){
            Profile profile=userQueryService.getuserprofile(new User(userId),false);
            profileInfo= Optional.ofNullable(cacheWriterService.cacheProfileInfo(profile));
        }
           if(profileInfo.isEmpty()){
               throw new InaccessibleObjectException("could not complete action please try later");
           }
          return usermapper.toSummary(profileInfo.get());
}

}
