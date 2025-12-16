package com.example.whatsappclone.IntegrationTests;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Exceptions.BadFollowRequestException;
import com.example.whatsappclone.Exceptions.UserNotFoundException;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.example.whatsappclone.Repositries.UserRepo;
import com.example.whatsappclone.Services.FollowService;
import com.example.whatsappclone.Services.UsersManagmentService;
import com.example.whatsappclone.UnitTests.FollowServiceTest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static com.example.whatsappclone.IntegrationTests.FollowTestHelper.assertthrows;
import  static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class FollowServiceIntegrationTest extends TestContainerConfig {

    private final FollowRepo followRepo;
    private final UserRepo userRepo;
    private final UsersManagmentService usersManagment;
    private final RedisTemplate<String,Object> redisTemplate;
    private final FollowService followService;
    private final FollowTestHelper followTestHelper;

    public enum RemovalType { REMOVE_FOLLOWER, UNFOLLOW }
    public  enum ProfileType { PRIVATE, PUBLIC }

    @BeforeEach
    public void setAuthentication(){
        User currentUser = userRepo.save(
                new User("Abdoumimi","Abderrahmane","Belkheir","abdoubelkhir63@gmail.com","azertyuiopqsdfghjklmwxcvbn"));
        Jwt jwt = Jwt.withTokenValue("test_token")
                .claim("sub", currentUser.getKeycloakId())
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
@CsvSource({
        "Follow.Status.PENDING",// current user already sent a follow request to this user
        "Follow.Status.ACCEPTED"// current user already follows this user
})
public void follow_recordFound_throwsBadFollowException(String followStatusString){
            Follow.Status followStatus=followStatusString.equals(Follow.Status.ACCEPTED.toString())? Follow.Status.ACCEPTED: Follow.Status.PENDING;
            User user=followTestHelper.createFollowRecord(followStatus);
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
        @CsvSource({
                "ProfileType.PRIVATE",// saves a Pending status follow record in db
                "ProfileType.PUBLIC"// saves an Accepted status follow record in db and cache
        })
        public void follow_profileTypes(String profileTypeString){
            ProfileType profileType=profileTypeString.equals(ProfileType.PRIVATE.toString())?ProfileType.PRIVATE:ProfileType.PUBLIC;
            User currentuser=usersManagment.getcurrentuser();
            User user=followTestHelper.followUser(profileType);
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
        @CsvSource({
                "Follow.Status.PENDING",// current user  sent a follow request to this user but not following him
                "Follow.Status.ACCEPTED"// current user follows this user // remove follow record from db and cache
        })
public void unfollow(String followStatusString){
            Follow.Status followStatus=followStatusString.equals(Follow.Status.ACCEPTED.toString())? Follow.Status.ACCEPTED: Follow.Status.PENDING;
           if(followStatus== Follow.Status.PENDING){
               assertthrows(BadFollowRequestException.class,
                       () -> followTestHelper.perfomeFollowRemoval(Follow.Status.PENDING, RemovalType.UNFOLLOW),
                       "you are not following this user try to unsend the request");
               return;
           }
            User user = followTestHelper.perfomeFollowRemoval(Follow.Status.ACCEPTED, RemovalType.UNFOLLOW);
            User currentUser = usersManagment.getcurrentuser();
            assertFalse(followRepo.existsByFollowerAndFollowing(currentUser, user));
            assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":following:" + user.getKeycloakId()));
            assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":follower:" + currentUser.getKeycloakId()));
}
        @ParameterizedTest
        @CsvSource({
                "Follow.Status.PENDING",// current user remove follower with pending status so not follower for the current user yet
                "Follow.Status.ACCEPTED"//current user remove follower  follow record removed from db and cache
        })
        public void removeFollower(String followStatusString){
            Follow.Status followStatus=followStatusString.equals(Follow.Status.ACCEPTED.toString())? Follow.Status.ACCEPTED: Follow.Status.PENDING;
            if(followStatus== Follow.Status.PENDING){
                assertthrows(BadFollowRequestException.class,
                        () -> followTestHelper.perfomeFollowRemoval(Follow.Status.PENDING, RemovalType.REMOVE_FOLLOWER),
                        "user not in followers try to reject the request");
                return;
            }
            User user = followTestHelper.perfomeFollowRemoval(Follow.Status.ACCEPTED, RemovalType.REMOVE_FOLLOWER);
            User currentUser = usersManagment.getcurrentuser();
            assertFalse(followRepo.existsByFollowerAndFollowing(user, currentUser));
            assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":following:" + currentUser.getKeycloakId()));
            assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":follower:" + user.getKeycloakId()));
        }
    }
}
