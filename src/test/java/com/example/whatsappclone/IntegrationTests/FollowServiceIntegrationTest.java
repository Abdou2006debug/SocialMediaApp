package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.RelationShipsServices.FollowRequestService;
import com.example.whatsappclone.Services.RelationShipsServices.FollowService;
import com.example.whatsappclone.Services.RelationShipsServices.UserFollowViewHelper;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

import static com.example.whatsappclone.IntegrationTests.FollowTestHelper.assertthrows;
import  static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class FollowServiceIntegrationTest extends TestContainerConfig {
    private final FollowRepo followRepo;
    private final UserRepo userRepo;
    private final RedisTemplate<String,Object> redisTemplate;
    private final FollowService followService;
    private final FollowTestHelper followTestHelper;
    private final UserQueryService userQueryService;
    private final FollowRequestService followRequestService;
    public  enum ProfileType { PRIVATE, PUBLIC }

    @BeforeEach
    public  void setAuthentication(){
        User currentUser = userRepo.save(
                new User("Abdoumimi","Abderrahmane","Belkheir","abdoubelkhir63@gmail.com"));
        Jwt jwt = Jwt.withTokenValue("test_token")
                .claim("userId", currentUser.getUuid())
                .claim("preferred_username", currentUser.getUsername())
                .header("alg","none")
                .build();
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Nested
    class FollowCreationTests {

        @Test
        @DisplayName("user not found")
        public void follow_nonExistingUser_throwsUserNotFoundException(){
            assertthrows(UserNotFoundException.class,
                    () -> followService.Follow("test"), "User not found");
        }


        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void follow_recordFound_throwsBadFollowException(Follow.Status followStatus){
            Map<String,Object> map=followTestHelper.createFollowRecord(followStatus, UserFollowViewHelper.Position.FOLLOWER);
            User user=(User)map.get("user");
            String expectedMessage=followStatus== Follow.Status.ACCEPTED?"Already followed":"request already sent";
    assertthrows(BadFollowRequestException.class,
            () -> followService.Follow(user.getUuid()), expectedMessage);
}

        @ParameterizedTest
        @CsvSource({
            "true",// current user is blocked by the user he wants to follow
                "false"// current user blocked the // // //   //    /   //
        })
        public void follow_blockFound_throwsBadFollowRequestException(boolean expected){
            String expectedMessage=expected?"User has blocked you":"You have blocked this user";
            User user = followTestHelper.createBlockRecord(expected);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(user.getUuid()), expectedMessage);
        }



        @ParameterizedTest
        @EnumSource(ProfileType.class)
        public void follow_profileTypes(ProfileType profileType){
            User currentuser=userQueryService.getcurrentuser();
            User user=followTestHelper.followUser(profileType);
            followService.Follow(user.getUuid());
            if(profileType==ProfileType.PUBLIC){
                assertTrue(followRepo.existsByFollowerAndFollowingAndStatus(currentuser, user, Follow.Status.ACCEPTED));
                assertTrue(redisTemplate.hasKey("user:" + currentuser.getKeycloakId() + ":following:" + user.getKeycloakId()));
                assertTrue(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":follower:" + currentuser.getKeycloakId()));
                return ;
            }
            assertTrue(followRepo.existsByFollowerAndFollowingAndStatus(currentuser, user, Follow.Status.PENDING));
        }
    }

    @Nested
    class FollowRemovalTests {
        @ParameterizedTest
      @EnumSource(Follow.Status.class)
        public void unfollow(Follow.Status followStatus){
            Map<String,Object> map=followTestHelper.createFollowRecord(followStatus, UserFollowViewHelper.Position.FOLLOWER);
            String followid=(String) map.get("followid");
           if(followStatus== Follow.Status.PENDING){
               assertthrows(BadFollowRequestException.class,
                       () -> followService.UnFollow(followid),
                       "you are not following this user try to unsend the request");
               return;
           }
            User user =(User) map.get("user");
            User currentUser = userQueryService.getcurrentuser();
            followService.UnFollow(followid);
            assertFalse(followRepo.existsByFollowerAndFollowing(currentUser, user));
            assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":following:" + user.getKeycloakId()));
            assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":follower:" + currentUser.getKeycloakId()));
}
        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void removeFollower(Follow.Status followStatus){
            Map<String,Object> map=followTestHelper.createFollowRecord(followStatus, UserFollowViewHelper.Position.FOLLOWING);
            String followid=(String) map.get("followid");
            if(followStatus== Follow.Status.PENDING){
                assertthrows(BadFollowRequestException.class,
                        () -> followService.removefollower(followid),
                        "user not in followers try to reject the request");
                return;
            }
            User user = (User) map.get("user");
            User currentUser = userQueryService.getcurrentuser();
            followService.removefollower(followid);
            assertFalse(followRepo.existsByFollowerAndFollowing(user, currentUser));
            assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":following:" + currentUser.getKeycloakId()));
            assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":follower:" + user.getKeycloakId()));
        }
    }
    @Nested
    class FollowRequestTests{
        @Test
        public void acceptFollowerDontExist(){
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptfollow("test"),"bad request");
        }
        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void acceptFollower(Follow.Status followStatus){
            Map<String,Object> map= followTestHelper.createFollowRecord(followStatus, UserFollowViewHelper.Position.FOLLOWING);
            String followid=(String)map.get("followid");
            if(followStatus== Follow.Status.ACCEPTED){
                assertthrows(BadFollowRequestException.class, () ->followRequestService.acceptfollow(followid),
                        "user already follow you");
                return;
            }
            User user=(User)map.get("user");
            followRequestService.acceptfollow(followid);
            User currentUser = userQueryService.getcurrentuser();
            assertTrue(followRepo.existsByFollowerAndFollowingAndStatus(user, currentUser, Follow.Status.ACCEPTED));
            assertTrue(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":following:" + currentUser.getKeycloakId()));
            assertTrue(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":follower:" + user.getKeycloakId()));
        }
        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void unsendFollow(Follow.Status followStatus){
            Map<String,Object> map= followTestHelper.createFollowRecord(followStatus, UserFollowViewHelper.Position.FOLLOWER);
            String followid=(String)map.get("followid");
            if(followStatus== Follow.Status.ACCEPTED){
                assertthrows(BadFollowRequestException.class, () ->followRequestService.unsendfollowingrequest(followid),
                        "you already follow this user");
                return;
            }
          User user=(User)map.get("user");
           User currentUser = userQueryService.getcurrentuser();
            followRequestService.unsendfollowingrequest(followid);
            assertFalse(followRepo.existsByFollowerAndFollowing(currentUser, user));
        }
    }
}
