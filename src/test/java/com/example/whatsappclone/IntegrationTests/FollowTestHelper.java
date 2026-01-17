package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.IntegrationTests.SoicalGraph.FollowServiceIntegrationTest;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.SocialGraph.application.FollowQueryHelper;
import com.example.whatsappclone.User.application.AuthenticatedUserService;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.User.persistence.UserRepo;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.SocialGraph.application.FollowService;
import com.example.whatsappclone.SocialGraph.domain.Blocks;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.BlocksRepo;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class FollowTestHelper {

    private final FollowRepo followRepo;
    private final FollowService followService;
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
        User currentuser=authenticatedUserService.getcurrentuser(false);
        Follow follow=position== FollowQueryHelper.Position.FOLLOWERS?
                new Follow(user,currentuser,status):new Follow(currentuser,user,status);
        followRepo.saveAndFlush(follow);
        return user;
    }

    public static void assertthrows(Class<? extends Exception> expected, Executable executable, String expectedMessage){
        Exception exception=assertThrows(expected,executable);
        assertEquals(expectedMessage,exception.getMessage());
    }

    public User createBlockRecord(boolean isCurrentBlocked){
        User user=createTestUser();
        User currentuser=authenticatedUserService.getcurrentuser(false);
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
        User user=new User();
        return userRepo.save(user);
    }
}
