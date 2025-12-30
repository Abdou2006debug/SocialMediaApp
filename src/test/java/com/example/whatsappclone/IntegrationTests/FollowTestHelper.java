package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.RelationShipsServices.FollowUtill;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
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
    private final UserQueryService userQueryService;
    private final FollowRepo followRepo;
    private final FollowService followService;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final UserRepo userRepo;


    public User followUser(FollowServiceIntegrationTest.ProfileType profiletype){
        User user=createTestUser();
        Profile profile=profiletype== FollowServiceIntegrationTest.ProfileType.PRIVATE?new Profile(true):new Profile(false);
        profile.setUser(user);
        profileRepo.save(profile);
        return user;
    }
    public Map<String,Object> createFollowRecord(Follow.Status status, FollowUtill.Position position){
        User user=createTestUser();
        User currentuser=userQueryService.getcurrentuser();
        Follow follow=position== FollowUtill.Position.FOLLOWER?
                new Follow(currentuser,user,status):new Follow(user,currentuser,status);
        followRepo.saveAndFlush(follow);
        Map<String,Object> map=new HashMap<>();
            map.put("user",user);
            map.put("followid",follow.getUuid());
        return map;
    }

    public static void assertthrows(Class<? extends Exception> expected, Executable executable, String expectedMessage){
        Exception exception=assertThrows(expected,executable);
        assertEquals(expectedMessage,exception.getMessage());
    }

    public User createBlockRecord(boolean isCurrentBlocked){
        User user=createTestUser();
        User currentuser=userQueryService.getcurrentuser();
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
        User user=new User("test","test","test","test@test.com","1234");
        return userRepo.save(user);
    }
}
