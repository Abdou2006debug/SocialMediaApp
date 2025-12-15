package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.BlockService;
import com.example.whatsappclone.Services.FollowService;
import com.example.whatsappclone.Services.UsersManagmentService;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class FollowTestHelper {
    private final UsersManagmentService usersManagment;
    private final FollowRepo followRepo;
    private final FollowService followService;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final UserRepo userRepo;
    public User perfomeFollowRemoval(Follow.Status status, FollowServiceIntegrationTest.RemovalType type){
        User currentuser=usersManagment.getcurrentuser();
        User user=createTestUser();
        Follow follow=type== FollowServiceIntegrationTest.RemovalType.UNFOLLOW?new Follow(currentuser,user,status):new Follow(user,currentuser,status);
        followRepo.save(follow);
        if(type== FollowServiceIntegrationTest.RemovalType.UNFOLLOW){
            followService.UnFollow(follow.getUuid());
        }else{
            followService.removefollower(follow.getUuid());
        }
        return user;
    }
    public User followUser(FollowServiceIntegrationTest.ProfileType profiletype){
        User user=createTestUser();
        Profile profile=profiletype== FollowServiceIntegrationTest.ProfileType.PRIVATE?new Profile(true):new Profile(false);
        profile.setUser(user);
        profileRepo.save(profile);
        followService.Follow(user.getUuid());
        return user;
    }

    public User createFollowRecord(Follow.Status status){
        User user=createTestUser();
        User currentuser=usersManagment.getcurrentuser();
        followRepo.saveAndFlush(new Follow(currentuser,user, status));
        return user;
    }
    public static void assertthrows(Class<? extends Exception> expected, Executable executable, String expectedMessage){
        Exception exception=assertThrows(expected,executable);
        assertEquals(expectedMessage,exception.getMessage());
    }

    public User createBlockRecord(boolean isCurrentBlocked){
        User user=createTestUser();
        User currentuser=usersManagment.getcurrentuser();
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
