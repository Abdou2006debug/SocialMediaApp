package com.example.whatsappclone.Services;

import com.example.whatsappclone.DTO.serverToclient.user;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FollowHelperService {
private final ApplicationEventPublisher applicationEventPublisher;
private final CachService cachService;
private final UserRepo userRepo;
private final ProfileRepo profileRepo;
private final UsersManagmentService usersManagment;
private final FollowRepo followRepo;
private final Logger logger= LoggerFactory.getLogger(FollowHelperService.class);
    public enum Position {FOLLOWER, FOLLOWING}
    public User GetUser(String keycloakid){
        User cacheduser=cachService.getUserbyKeycloakId(keycloakid);
        return   cacheduser==null?userRepo.findByKeycloakId(keycloakid).orElseThrow(()->new UserNotFoundException("user not found")):cacheduser;
    }
    public user buildUser(User u, String followId) {
        Profile cached = cachService.getcachedprofile(u);
        Profile profile = cached == null ? profileRepo.findByUser(u).orElse(null) : cached;
        String avatar = profile == null ? null : profile.getPublicavatarurl();
        return new user(u.getUuid(), u.getUsername(), avatar, followId);
    }
    public List<user> ListFollows(Position followposition, Follow.Status followstatus,int page,User currentuser){
        Map<String,String> followmap=null;
        if(followstatus== Follow.Status.ACCEPTED){
            followmap=followposition== Position.FOLLOWER?
                    cachService.getusercachedfollowers(currentuser,page):
                    cachService.getusercachedfollowings(currentuser,page);
        }
        if(followstatus== Follow.Status.PENDING||followmap==null){
            if(followstatus== Follow.Status.ACCEPTED){
                if(followposition== Position.FOLLOWING){
                        cachService.cachuserfollowings(currentuser,page);
                   // applicationEventPublisher.publishEvent();
                }else{
                        cachService.cachuserfollowers(currentuser,page);
                }
            }
            List<Follow> followList=followposition== Position.FOLLOWER?
                    followRepo.findByFollowingAndStatus(currentuser, followstatus,PageRequest.of(page,10)).getContent():
                    followRepo.findByFollowerAndStatus(currentuser,followstatus,PageRequest.of(page,10)).getContent();
            return followList.stream().map(follow -> {
                String followid=follow.getUuid();
                User followingOrfollower=followposition== Position.FOLLOWER?
                        follow.getFollower():follow.getFollowing();
                return buildUser(followingOrfollower,followid);
            }).toList();
        }
        logger.info("getting from cach");
        List<user> follows=new LinkedList<>();
        followmap.forEach((followid,keycloakid)->{
            User user= GetUser(keycloakid);
            follows.add(buildUser(user,followid));
        });
        return follows;
    }
}
