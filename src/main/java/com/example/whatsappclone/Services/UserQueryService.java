package com.example.whatsappclone.Services;

import com.example.whatsappclone.DTO.serverToclient.account;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Services.CachService;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final CachService cachService;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final FollowRepo followRepo;
    public Profile getuserprofile(User requesteduser,Boolean cachit){
        Profile cached = cachService.getcachedprofile(requesteduser);
        Profile profile = cached == null ? profileRepo.findByUser(requesteduser).get() : cached;
        if(cachit&&cached==null){
            cachService.cachuserprofile(profile);
        }
        return profile;
    }

    public User getcurrentuser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null||!(authentication.getPrincipal() instanceof Jwt)){
            return null;
        }
        String keycloakid=((Jwt) authentication.getPrincipal()).getSubject();
        User cachedtuser=cachService.getUserbyKeycloakId(keycloakid);
        if(cachedtuser!=null){
            return cachedtuser;
        }
        User user=  userRepo.findByKeycloakId(keycloakid).
                orElseThrow(()->new UserNotFoundException("user not found"));
        cachService.cachuser(user);
        return user;
    }
    public account getuser(String useruuid){
        User currentuser=getcurrentuser();
        User requesteduser;
        requesteduser=cachService.getUserbyId(useruuid);
        if (requesteduser == null) {
            requesteduser= userRepo.findById(useruuid).
                    orElseThrow(() -> new UserNotFoundException("user not found"));
        }
        Profile profile=getuserprofile(requesteduser,false);
        String status=null;

        boolean hasblocked= blocksRepo.
                existsByBlockedAndBlocker(requesteduser,currentuser);
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
        return new account(requesteduser.getUuid(),requesteduser.getUsername(),
                profile.getPublicavatarurl(),profile.getBio(),status,followersCount(requesteduser),followingsCount(requesteduser),lastseen, isonline);
    }
    public long followersCount(User user){
        return followRepo.countByFollowingAndStatus(user, Follow.Status.ACCEPTED);

    }
    public long followingsCount(User user){
        return followRepo.countByFollowerAndStatus(user, Follow.Status.ACCEPTED);
    }
}
