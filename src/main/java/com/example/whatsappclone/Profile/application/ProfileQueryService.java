package com.example.whatsappclone.Profile.application;

import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Profile.api.dto.profileDetails;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.application.cache.ProfileCacheManager;
import com.example.whatsappclone.Profile.domain.Profile;


import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Services.CacheServices.CacheQueryService;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Shared.CheckUserExistence;
import com.example.whatsappclone.Shared.Mappers.Profilemapper;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.domain.RelationshipStatus;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileQueryService {

    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileCacheManager profileCacheManager;
    private final ProfileRepo profileRepo;
    private final Profilemapper profilemapper;
    private final FollowRepo followRepo;
    private final BlocksRepo blocksRepo;

    @CheckUserExistence
    public profileDetails getUserProfile(String userId){
       User currentUser=authenticatedUserService.getcurrentuser(false);
       User targetUser=new User(userId);
     ProfileInfo profileInfo= profileCacheManager.getProfileInfo(userId).orElseGet(()->{
         Profile profile=getuserprofile(new User(userId),false);
         profileCacheManager.cacheProfileInfo(profile);
         return profilemapper.toprofileInfo(profile);
     });
        profileDetails profileDetails=profilemapper.toprofileDetails(profileInfo);

        profileDetails.setFollowers(followRepo.countByFollowingAndStatus(targetUser, Follow.Status.ACCEPTED));
        profileDetails.setFollowings(followRepo.countByFollowerAndStatus(targetUser, Follow.Status.ACCEPTED));

        if(userId.equals(currentUser.getUuid())){
            return profileDetails;
        }

     if(blocksRepo.existsByBlockerAndBlocked(currentUser,targetUser)||blocksRepo.existsByBlockerAndBlocked(targetUser,currentUser)){
         profileDetails.setBio(null);
         profileDetails.setAvatarurl(null);
         profileDetails.setUsername("Instagram User");
         return profileDetails;
     }


     RelationshipStatus status=RelationshipStatus.NOT_FOLLOWING;

        if(followRepo.existsByFollowerAndFollowingAndStatus(currentUser,targetUser, Follow.Status.ACCEPTED)){
            status= RelationshipStatus.FOLLOWING;
        }else if(followRepo.existsByFollowerAndFollowingAndStatus(currentUser,targetUser, Follow.Status.PENDING)){
            status=RelationshipStatus.FOLLOW_REQUESTED;
        }else if(followRepo.existsByFollowerAndFollowingAndStatus(targetUser,currentUser, Follow.Status.ACCEPTED)){
            status=RelationshipStatus.FOLLOWED;
        }else if(followRepo.existsByFollowerAndFollowingAndStatus(targetUser,currentUser, Follow.Status.PENDING)){
            status=RelationshipStatus.FOLLOW_REQUEST_RECEIVED;
        }
      profileDetails.setStatus(status);
        return profileDetails;
    }

    public profilesettings getMyProfileSettings(){
        User currentuser=authenticatedUserService.getcurrentuser(false);
        Profile profile=getuserprofile(currentuser,true);
        return profilemapper.toprofilesettings(profile);
    }

    public Profile getuserprofile(User user, Boolean cacheProfile){
        Optional<Profile> cached = profileCacheManager.getProfile(user.getUuid());
        Profile profile = cached.orElseGet(() -> profileRepo.findByUser(user).orElseThrow());
        if(cacheProfile &&cached.isEmpty()){
            profileCacheManager.cacheUserProfile(profile);
        }
        return profile;
    }

}
