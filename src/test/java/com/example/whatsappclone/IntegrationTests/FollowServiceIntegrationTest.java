package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.CachService;
import com.example.whatsappclone.Services.FollowService;
import com.example.whatsappclone.Services.UsersManagmentService;
import com.example.whatsappclone.UnitTests.ServicesTests.FollowServiceTest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class FollowServiceIntegrationTest extends TestContainerConfig {

    private final FollowRepo followRepo;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final UsersManagmentService usersManagment;
    private final BlocksRepo blocksRepo;
    private final CachService cachService;
    private final FollowService followService;
    private final ApplicationEventPublisher eventPublisher;

    @BeforeEach
    public void setAuthentication(){
        User currentuser=userRepo.save(
                new User("Abdoumimi","Abderrahmane","Belkheir","abdoubelkhir63@gmail.com","azertyuiopqsdfghjklmwxcvbn"));
        Jwt jwt=Jwt.withTokenValue("test_token").claim("sub",currentuser.getKeycloakId()).claim("preferred_username",currentuser.getUsername()).header("alg","none").build();
        JwtAuthenticationToken authenticationToken=new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
    @Nested
    class FollowTesting{
     @Test
     @DisplayName("current already follow user")
        public void UserAlreadyFollowed(){
     User user= followRecordFound(Follow.Status.ACCEPTED);
     FollowServiceTest.
     assertthrows(BadFollowRequestException.class,()->followService.Follow(user.getUuid()),"Already followed");
     }
     @Test
     @DisplayName("current already send request to user")
        public void RequestAlreadySent(){
         User user=followRecordFound(Follow.Status.PENDING);
        FollowServiceTest.
                 assertthrows(BadFollowRequestException.class,()->followService.Follow(user.getUuid()),"request already sent");
     }

     @Test
     @DisplayName("the current has blocked the user he wants to follow so currentblocked==false")
     public void CurrentUserBlocker(){
       User user=  blockRecordFound(false);
         FollowServiceTest.assertthrows(BadFollowRequestException.class,()->followService.Follow(user.getUuid()),"You have blocked this user");
     }
     @Test
     @DisplayName("the user the current wants to follow has blocked him so currentblocked==true")
     public void CurrentUserBlocked(){
        User user= blockRecordFound(true);
         FollowServiceTest.assertthrows(BadFollowRequestException.class,()->followService.Follow(user.getUuid()),"User has blocked you");
     }
    }


    private User followRecordFound(Follow.Status status){
        User user=createTestUser();
        User currentuser=usersManagment.getcurrentuser();
        Follow follow=followRepo.saveAndFlush(new Follow(currentuser,user, status));
        return user;
    }


    private User blockRecordFound(boolean isCurrentBlocked){
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
        return userRepo.save(new User("test"));
    }
}
