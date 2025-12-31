package com.example.whatsappclone.Services.UserManagmentServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
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
        User currentuser=getcurrentuser(false);
        User requesteduser=new User(requestedId);
        if (!userRepo.existsById(requestedId)) {
           throw new UserNotFoundException("user not found");
        }
        ProfileInfo profileInfo=cacheQueryService.getProfileInfo(requestedId);
        if(profileInfo==null){
            Profile profile=getuserprofile(requesteduser,false);
            profileInfo=cachService.cacheProfileInfo(profile);
        }
        String status=null;

        boolean hasblocked= blocksRepo.
                existsByBlockerAndBlocked(currentuser,requesteduser);
        if(hasblocked){
            status="you have blocked him";
        }
        boolean isfolloweraccepted = followRepo.
                existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.ACCEPTED);
        boolean isfollowerpending=followRepo.
                existsByFollowerAndFollowingAndStatus(currentuser,requesteduser, Follow.Status.PENDING);
        if(isfolloweraccepted){
            status="following";
        }else if(isfollowerpending){
            status="sent";
        }else{
            boolean isfollowingaccepted=followRepo.
                    existsByFollowerAndFollowingAndStatus(requesteduser,currentuser, Follow.Status.ACCEPTED);
            if(isfollowingaccepted){
                status="follow back";
            }
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
