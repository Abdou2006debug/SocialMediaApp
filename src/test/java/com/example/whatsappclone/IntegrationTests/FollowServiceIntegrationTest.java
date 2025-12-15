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
            FollowTestHelper.assertthrows(UserNotFoundException.class,
                    () -> followService.Follow("test"), "User not found");
        }

        @Test
        @DisplayName("current already follow user")
        public void follow_alreadyFollowedUser_throwsBadFollowRequestException(){
            User user = followTestHelper.createFollowRecord(Follow.Status.ACCEPTED);
           FollowTestHelper.assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(user.getUuid()), "Already followed");
        }

        @Test
        @DisplayName("current already sent request to user")
        public void follow_pendingFollowRequest_throwsBadFollowRequestException(){
            User user = followTestHelper.createFollowRecord(Follow.Status.PENDING);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(user.getUuid()), "request already sent");
        }

        @Test
        @DisplayName("the current has blocked the user he wants to follow so currentblocked==false")
        public void follow_userBlockedByCurrentUser_throwsBadFollowRequestException(){
            User user = followTestHelper.createBlockRecord(false);
           assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(user.getUuid()), "You have blocked this user");
        }

        @Test
        @DisplayName("the user the current wants to follow has blocked him so currentblocked==true")
        public void follow_userHasBlockedCurrentUser_throwsBadFollowRequestException(){
            User user = followTestHelper.createBlockRecord(true);
           assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(user.getUuid()), "User has blocked you");
        }

        @Test
        @DisplayName("user successfully followed other user checking if record was saved in db and also making sure that the cache was hit")
        public void follow_publicProfile_savesFollowAndUpdatesCache(){
            User currentUser = usersManagment.getcurrentuser();
            User user = followTestHelper.followUser(ProfileType.PUBLIC);
            assertTrue(followRepo.existsByFollowerAndFollowingAndStatus(currentUser, user, Follow.Status.ACCEPTED));
            assertTrue(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":following:" + user.getKeycloakId()));
            assertTrue(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":follower:" + currentUser.getKeycloakId()));
        }

        @Test
        @DisplayName("user successfully sent request to other user checking if record was saved in db")
        public void follow_privateProfile_savesPendingRequest(){
            User currentUser = usersManagment.getcurrentuser();
            User user = followTestHelper.followUser(ProfileType.PRIVATE);
            assertTrue(followRepo.existsByFollowerAndFollowingAndStatus(currentUser, user, Follow.Status.PENDING));
        }
    }

    @Nested
    class FollowRemovalTests {

        @Test
        public void unfollow_pendingFollow_throwsBadFollowRequestException(){
            assertthrows(BadFollowRequestException.class,
                    () -> followTestHelper.perfomeFollowRemoval(Follow.Status.PENDING, RemovalType.UNFOLLOW),
                    "you are not following this user try to unsend the request");
        }

        @Test
        public void unfollow_acceptedFollow_deletesFollowAndClearsCache(){
            User user = followTestHelper.perfomeFollowRemoval(Follow.Status.ACCEPTED, RemovalType.UNFOLLOW);
            User currentUser = usersManagment.getcurrentuser();
            assertFalse(followRepo.existsByFollowerAndFollowing(currentUser, user));
            assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":following:" + user.getKeycloakId()));
            assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":follower:" + currentUser.getKeycloakId()));
        }

        @Test
        public void removeFollower_pendingFollow_throwsBadFollowRequestException(){
            assertthrows(BadFollowRequestException.class,
                    () -> followTestHelper.perfomeFollowRemoval(Follow.Status.PENDING, RemovalType.REMOVE_FOLLOWER),
                    "user not in followers try to reject the request");
        }

        @Test
        public void removeFollower_acceptedFollow_deletesFollowAndClearsCache(){
            User user = followTestHelper.perfomeFollowRemoval(Follow.Status.ACCEPTED, RemovalType.REMOVE_FOLLOWER);
            User currentUser = usersManagment.getcurrentuser();
            assertFalse(followRepo.existsByFollowerAndFollowing(user, currentUser));
            assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":following:" + currentUser.getKeycloakId()));
            assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":follower:" + user.getKeycloakId()));
        }
    }
}
