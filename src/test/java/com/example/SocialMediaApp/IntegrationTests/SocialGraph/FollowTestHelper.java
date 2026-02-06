package com.example.SocialMediaApp.IntegrationTests.SocialGraph;

import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.SocialGraph.application.FollowQueryHelper;
import com.example.SocialMediaApp.SocialGraph.domain.Blocks;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class FollowTestHelper {

    private final FollowRepo followRepo;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;


    public User followUser(FollowServiceIntegrationTest.ProfileType profiletype){
        User user=createTestUser();
        Profile profile=profiletype== FollowServiceIntegrationTest.ProfileType.PRIVATE?new Profile(true):new Profile(false);
        profile.setUser(user);
        profileRepo.save(profile);
        return user;
    }

    public User createFollowRecord(Follow.Status status, FollowQueryHelper.Position position){
        User user=createTestUser();
        User currentuser=authenticatedUserService.getcurrentuser();
        Follow follow=position== FollowQueryHelper.Position.FOLLOWERS?
                new Follow(user,currentuser,status):new Follow(currentuser,user,status);
        followRepo.saveAndFlush(follow);
        return user;
    }

    public User createBlockRecord(boolean isCurrentBlocked){
        User user=createTestUser();
        User currentuser=authenticatedUserService.getcurrentuser();
        Blocks blocks=new Blocks();
        if(isCurrentBlocked){
            blocks.setBlocked(currentuser);
            blocks.setBlocker(user);
        }else{
            blocks.setBlocked(user);
            blocks.setBlocker(currentuser);
        }
        blocksRepo.saveAndFlush(blocks);
        return user;
    }


    public User createTestUser(){
        User user=new User(UUID.randomUUID().toString());
        return userRepo.saveAndFlush(user);
    }
}
