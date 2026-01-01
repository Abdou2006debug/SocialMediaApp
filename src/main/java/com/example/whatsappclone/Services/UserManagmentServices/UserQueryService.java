package com.example.whatsappclone.Services.UserManagmentServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.DTO.serverToclient.RelationshipStatus;
import com.example.whatsappclone.DTO.serverToclient.profileDetails;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.CacheServices.CacheQueryService;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final CacheWriterService cachService;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final FollowRepo followRepo;
    private final CacheQueryService cacheQueryService;
    public Profile getuserprofile(User user,Boolean cacheProfile){
        Optional<Profile> cached = cacheQueryService.getProfile(user.getUuid());
        Profile profile = cached.orElseGet(() -> profileRepo.findByUser(user).orElseThrow());
        if(cacheProfile &&cached.isEmpty()){
            cachService.cacheUserProfile(profile);
        }
        return profile;
    }

    public User getcurrentuser(boolean fetchfullinfo){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null||!(authentication.getPrincipal() instanceof Jwt)){
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        String userId=((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
        if(userId==null){
            throw new AuthenticationCredentialsNotFoundException("something went wrong trying to authenticate you please try later");
        }
        if(fetchfullinfo){
            Optional<User> cacheduser=cacheQueryService.getUser(userId);
            if(cacheduser.isPresent()){
                return cacheduser.get();
            }
            User user=  userRepo.findById(userId).
                    orElseThrow(()->new UserNotFoundException("user not found"));
            cachService.cacheUser(user);
            return user;
        }
        return new User(userId);
    }
    public profileDetails getuser(String requestedId){
        if (!userRepo.existsById(requestedId)) {
            throw new UserNotFoundException("user not found");
        }
        User currentuser=getcurrentuser(false);
        User requesteduser=new User(requestedId);
        ProfileInfo  profileInfo=cacheQueryService.getProfileInfo(requestedId).orElseGet(()->{
            Profile profile=getuserprofile(requesteduser,false);
            return  cachService.cacheProfileInfo(profile);
        });
        RelationshipStatus status;

        if(followRepo.existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.ACCEPTED)){
            status= RelationshipStatus.FOLLOWING;
        }else if(followRepo.existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.PENDING)){
            status=RelationshipStatus.FOLLOW_REQUESTED;
        }else if(followRepo.existsByFollowerAndFollowingAndStatus(requesteduser,currentuser, Follow.Status.ACCEPTED)){
            status=RelationshipStatus.FOLLOWED;
        }else if(followRepo.existsByFollowerAndFollowingAndStatus(requesteduser,currentuser, Follow.Status.PENDING)){
            status=RelationshipStatus.FOLLOW_REQUEST_RECEIVED;
        }

        boolean isonline=cachService.getuserstatus(requesteduser.getUsername());
        String lastseen=isonline?null: cachService.getuserlastseen(requesteduser.getUsername());
        return new profileDetails(requesteduser.getUuid(),requesteduser.getUsername(),
                profileInfo.getPfpurl(),profileInfo.getBio(),status,followersCount(requesteduser),followingsCount(requesteduser),lastseen, isonline);
    }
    public long followersCount(User user){
        return followRepo.countByFollowingAndStatus(user, Follow.Status.ACCEPTED);

    }
    public long followingsCount(User user){
        return followRepo.countByFollowerAndStatus(user, Follow.Status.ACCEPTED);
    }
}
