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
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class FollowUtill {
private final ApplicationEventPublisher applicationEventPublisher;
private final CachService cachService;
private final UserRepo userRepo;
private final ProfileRepo profileRepo;
private final UsersManagmentService usersManagment;
private final FollowRepo followRepo;
private final Logger logger= LoggerFactory.getLogger(FollowUtill.class);
    private final ConcurrentHashMap<String, Object> cacheLocks = new ConcurrentHashMap<>();
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

    public List<user> ListMyFollows_Accepted(Position position,int page,User requesteduser){
        String lockKey = requesteduser.getUuid() + ":" + position + ":" + page;
        Object lock = cacheLocks.computeIfAbsent(lockKey, k -> new Object());

        synchronized (lock){
       Map<String,String> followmap=position== Position.FOLLOWER?
                cachService.getusercachedfollowers(requesteduser,page):
                cachService.getusercachedfollowings(requesteduser,page);
       if(followmap==null){
           if(position== Position.FOLLOWING){
               cachService.cachuserfollowings(requesteduser,page);
               followmap=cachService.getusercachedfollowings(requesteduser,page);
           }else{
               cachService.cachuserfollowers(requesteduser,page);
               followmap=cachService.getusercachedfollowers(requesteduser,page);
       }
       }
            List<user> follows=new LinkedList<>();
            followmap.forEach((followid,keycloakid)->{
                User user= GetUser(keycloakid);
                follows.add(buildUser(user,followid));
            });
            return follows;
        }
       }
public List<user> ListOtherFollows(Position position,int page ,User requestuser){
  return helper(position,page,requestuser, Follow.Status.ACCEPTED);

}
private List<user> helper(Position position,int page,User requestuser,Follow.Status status){
    List<Follow> followList=position== Position.FOLLOWER?
            followRepo.findByFollowingAndStatus(requestuser, status,PageRequest.of(page,10)).getContent():
            followRepo.findByFollowerAndStatus(requestuser, status,PageRequest.of(page,10)).getContent();
    return followList.stream().map(follow -> {
        String followid=follow.getUuid();
        User followingOrfollower=position== Position.FOLLOWER?
                follow.getFollower():follow.getFollowing();
        return buildUser(followingOrfollower,followid);
    }).toList();
}
       public List<user> ListMyFollows_Pending(Position position,int page,User requesteduser){
           return helper(position,page,requesteduser, Follow.Status.PENDING);
        }
}
