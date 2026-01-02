package com.example.whatsappclone.Services.RelationShipsServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.DTO.serverToclient.RelationshipStatus;
import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.FollowListNotVisibleException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Services.CacheServices.CacheQueryService;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import com.example.whatsappclone.Services.UserManagmentServices.Usermapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
private final ProfileRepo profileRepo;
    public enum Position {FOLLOWERS, FOLLOWINGS}



    public List<profileSummary> listCurrentUserFollows(String userId, Position position, int page){
     List<profileSummary> profileSummaries=getProfileSummaries(userId,position,page,true);
     return profileSummaries.stream().map(profile -> {
        profileSummary profileSummary= buildProfileSummary(profile.getUserId());
             resolveCurrentUserFollowRelationShip(profileSummary,userId,position);
         return profileSummary;
     }).toList();
       }

       public List<profileSummary> listUserFollows(String viewerId,String targetedId, Position position, int page){
           List<profileSummary> profileSummaries=getProfileSummaries(targetedId,position,page,false);
          List<profileSummary> profileSummaries1=  profileSummaries.stream().map(profile -> buildProfileSummary(profile.getUserId())
           ).toList();
            return resolveUserFollowRelationShip(profileSummaries1,viewerId);
       }
       private void resolveCurrentUserFollowRelationShip(profileSummary profile, String userId, Position position){
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
    private List<profileSummary> resolveUserFollowRelationShip(List<profileSummary> profileSummaries, String viewerId) {
        Map<String, profileSummary> summaryMap = profileSummaries.stream()
                .collect(Collectors.toMap(profileSummary::getUserId, Function.identity()));

        List<String> targetUserIds = profileSummaries.stream()
                .map(profileSummary::getUserId)
                .toList();

        List<Follow> outgoing = followRepo.findByFollower_IdAndFollowing_IdIn(viewerId, targetUserIds);
        for (Follow follow : outgoing) {
            profileSummary summary = summaryMap.get(follow.getFollowing_id());
            if (summary != null) {
                summary.setFollowId(follow.getUuid());
                RelationshipStatus status = follow.getStatus() == Follow.Status.PENDING
                        ? RelationshipStatus.FOLLOW_REQUESTED
                        : RelationshipStatus.FOLLOWING;
                summary.setStatus(status);
            }
        }


        List<Follow> incoming = followRepo.findByFollowing_IdAndFollower_IdIn(viewerId,targetUserIds);
        for (Follow follow : incoming) {
            profileSummary summary = summaryMap.get(follow.getFollower_id());
            if (summary != null && summary.getStatus() == null) {
                summary.setFollowId(follow.getUuid());
                RelationshipStatus status = follow.getStatus() == Follow.Status.PENDING
                        ? RelationshipStatus.FOLLOW_REQUEST_RECEIVED
                        : RelationshipStatus.FOLLOWED;
                summary.setStatus(status);
            }
        }


        summaryMap.values().forEach(summary -> {
            if (summary.getStatus() == null) {
                summary.setStatus(RelationshipStatus.NOT_FOLLOWING);
            }
        });
        return summaryMap.values().stream().toList();
    }

    private profileSummary buildProfileSummary(String userId){
        ProfileInfo profileInfocache = cacheQueryService.getProfileInfo(userId).orElseGet(()->{
                Profile profile=userQueryService.getuserprofile(new User(userId),false);
           return cacheWriterService.cacheProfileInfo(profile);
        });
          return usermapper.toSummary(profileInfocache);
}
        private List<profileSummary> getProfileSummaries(String userId,Position position,int page,boolean fetchFollowId){
           if(position==Position.FOLLOWERS){
               return cacheQueryService.getuserCachedFollowers(userId,page, fetchFollowId).orElseGet(()->cacheWriterService.cachUserFollowers(userId,page));
           }
            return cacheQueryService.getusercachedfollowings(userId,page, fetchFollowId).orElseGet(()->cacheWriterService.cachUserFollowings(userId,page));
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

            if(!profileRepo.existsByUserAndIsprivateFalse(requestedUser)){
                if(!followRepo.existsByFollowerAndFollowingAndStatus(currentUser,requestedUser, Follow.Status.ACCEPTED)){
                    throw new FollowListNotVisibleException(String.format("%s list for user is not visible: the profile is private.",position.name()));
                };
            }

        }
}

